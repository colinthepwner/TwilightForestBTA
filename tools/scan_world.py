"""Read a generated BTA world back off disk and count what actually generated.

The startup checkpoints prove a decoration *registered*; they cannot prove a feature placed a
block, and eyeballing a world cannot tell "none" from "not here yet". This closes that gap by
reading the region files directly, which is the only check in the project that tests generation
rather than registration (PORTING-PLAYBOOK.md section 6a).

BTA's McRegion chunks are NBT with four deviations, each of which silently desyncs a vanilla-NBT
reader rather than failing:

  * tag 11 is short[], not int[]        -- HeightMap otherwise reads at half length
  * tag 12 is double[]                  -- HumidityMap otherwise desyncs 6 bytes per entry
  * every short array is LITTLE-endian  -- Blocks otherwise reads as id << 8
  * sections key on "yPos", not "Y"     -- every block otherwise lands in section 0

BiomeMap is two 256-byte planes rather than 256 shorts; plane 0 is the biome id per column, one
byte each, which is why the id ceiling is 255.

Usage:
    python tools/scan_world.py <world-dir> [--dim 0] [--blocks 1830,1831] [--top 40]

Reports, per biome: column count, surface height distribution, and the blocks named by --blocks
broken down by Y band. With no --blocks it prints the full block census.
"""

import argparse
import collections
import gzip
import io
import os
import struct
import sys
import zlib

TAG_END, TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG = 0, 1, 2, 3, 4
TAG_FLOAT, TAG_DOUBLE, TAG_BYTE_ARRAY, TAG_STRING = 5, 6, 7, 8
TAG_LIST, TAG_COMPOUND, TAG_SHORT_ARRAY, TAG_DOUBLE_ARRAY = 9, 10, 11, 12


class Reader:
    def __init__(self, data):
        self.d = data
        self.i = 0

    def take(self, n):
        b = self.d[self.i:self.i + n]
        if len(b) != n:
            raise EOFError("truncated NBT")
        self.i += n
        return b

    def u1(self):
        return self.take(1)[0]

    def i1(self):
        return struct.unpack(">b", self.take(1))[0]

    def i2(self):
        return struct.unpack(">h", self.take(2))[0]

    def u2(self):
        return struct.unpack(">H", self.take(2))[0]

    def i4(self):
        return struct.unpack(">i", self.take(4))[0]

    def i8(self):
        return struct.unpack(">q", self.take(8))[0]

    def f4(self):
        return struct.unpack(">f", self.take(4))[0]

    def f8(self):
        return struct.unpack(">d", self.take(8))[0]

    def string(self):
        return self.take(self.u2()).decode("utf-8", "replace")


def read_payload(r, tag):
    if tag == TAG_BYTE:
        return r.i1()
    if tag == TAG_SHORT:
        return r.i2()
    if tag == TAG_INT:
        return r.i4()
    if tag == TAG_LONG:
        return r.i8()
    if tag == TAG_FLOAT:
        return r.f4()
    if tag == TAG_DOUBLE:
        return r.f8()
    if tag == TAG_BYTE_ARRAY:
        return r.take(r.i4())
    if tag == TAG_STRING:
        return r.string()
    if tag == TAG_LIST:
        inner = r.u1()
        n = r.i4()
        return [read_payload(r, inner) for _ in range(max(0, n))]
    if tag == TAG_COMPOUND:
        out = {}
        while True:
            t = r.u1()
            if t == TAG_END:
                return out
            # The name must be read before the payload. Written as one statement
            # (out[r.string()] = read_payload(r, t)) Python evaluates the payload first and the
            # whole file desyncs from the first compound onwards.
            key = r.string()
            out[key] = read_payload(r, t)
    if tag == TAG_SHORT_ARRAY:
        # DEVIATION: short[], and LITTLE-endian.
        n = r.i4()
        return struct.unpack("<%dh" % n, r.take(n * 2))
    if tag == TAG_DOUBLE_ARRAY:
        # DEVIATION: double[], big-endian like every other float in NBT.
        n = r.i4()
        return struct.unpack(">%dd" % n, r.take(n * 8))
    raise ValueError("unknown NBT tag %d" % tag)


def read_nbt(data):
    r = Reader(data)
    tag = r.u1()
    if tag == TAG_END:
        return {}
    r.string()
    return read_payload(r, tag)


def decompress(scheme, blob):
    if scheme == 1:
        return gzip.decompress(blob)
    if scheme == 2:
        return zlib.decompress(blob)
    return blob


def region_chunks(path):
    with open(path, "rb") as fh:
        header = fh.read(4096)
        if len(header) < 4096:
            return
        fh.read(4096)
        body = fh.read()
    for slot in range(1024):
        word = struct.unpack(">I", header[slot * 4:slot * 4 + 4])[0]
        offset, sectors = word >> 8, word & 0xFF
        if offset == 0 or sectors == 0:
            continue
        start = offset * 4096 - 8192
        if start < 0 or start + 5 > len(body):
            continue
        length = struct.unpack(">i", body[start:start + 4])[0]
        scheme = body[start + 4]
        blob = body[start + 5:start + 4 + length]
        try:
            yield read_nbt(decompress(scheme, blob))
        except Exception as exc:            # a partly-written chunk is not a reason to stop
            print("  ! chunk in %s slot %d: %s" % (os.path.basename(path), slot, exc),
                  file=sys.stderr)


def level_of(chunk):
    return chunk.get("Level", chunk)


def biome_names(chunk):
    """Registries/Biomes, inverted to id -> key.

    Every chunk carries its own copy: ChunkWriter.putBiomeRegistry writes
    Registry.writeIdMapToTag(Registries.BIOMES, ...) as a key -> numeric-id compound, so a chunk
    saved by an older roster can still be decoded. It is NOT in level.dat or dimension.dat -- and
    looking for it there returns nothing, which reads exactly like "this world has no biomes" and
    leaves every id in the report as a bare number.
    """
    for holder in (chunk, chunk.get("Level") or {}):
        registries = holder.get("Registries")
        if isinstance(registries, dict) and isinstance(registries.get("Biomes"), dict):
            return {num: key for key, num in registries["Biomes"].items()
                    if isinstance(num, int)}
    return {}


def scan(world_dir, dim, wanted, sea_level):
    region_dir = os.path.join(world_dir, "dimensions", str(dim), "region")
    if not os.path.isdir(region_dir):
        region_dir = os.path.join(world_dir, "region")
    if not os.path.isdir(region_dir):
        raise SystemExit("no region directory under %s" % world_dir)

    names = {}
    census = collections.Counter()                       # block id -> count
    per_biome = collections.defaultdict(collections.Counter)
    biome_columns = collections.Counter()
    surface = collections.defaultdict(list)              # biome -> [surface y]
    water_columns = collections.Counter()                # biome -> columns whose surface < sea
    band = collections.defaultdict(collections.Counter)  # (block, y//16) -> count
    chunks = 0
    populated = 0

    for entry in sorted(os.listdir(region_dir)):
        if not entry.endswith(".mcr"):
            continue
        for chunk in region_chunks(os.path.join(region_dir, entry)):
            level = level_of(chunk)
            sections = level.get("Sections") or []
            if not sections:
                continue
            if not names:
                names = biome_names(chunk)
            chunks += 1
            if level.get("TerrainPopulated", 0):
                populated += 1

            # Column biome: plane 0 of any section's BiomeMap, one byte per column.
            column_biome = None
            for section in sections:
                bm = section.get("BiomeMap")
                if bm and len(bm) >= 256:
                    column_biome = bm[:256]
                    break

            # Highest non-air block per column, for the surface distribution.
            highest = [-1] * 256
            for section in sections:
                y_pos = section.get("yPos")             # DEVIATION: yPos, not Y
                blocks = section.get("Blocks")
                if y_pos is None or not blocks:
                    continue
                base = y_pos * 16
                for index, block in enumerate(blocks):
                    if block == 0:
                        continue
                    bid = block & 0xFFFF
                    census[bid] += 1
                    y = base + (index >> 8)
                    x = index & 15
                    z = (index >> 4) & 15
                    col = z * 16 + x
                    name = None
                    if column_biome is not None:
                        name = names.get(column_biome[col] & 0xFF, column_biome[col] & 0xFF)
                        per_biome[name][bid] += 1
                    if not wanted or bid in wanted:
                        band[bid][y // 16] += 1
                    if bid not in TRANSPARENT and y > highest[col]:
                        highest[col] = y

            for col in range(256):
                if highest[col] < 0:
                    continue
                name = None
                if column_biome is not None:
                    name = names.get(column_biome[col] & 0xFF, column_biome[col] & 0xFF)
                biome_columns[name] += 1
                surface[name].append(highest[col])
                if highest[col] < sea_level:
                    water_columns[name] += 1

    return {
        "names": names, "census": census, "per_biome": per_biome,
        "columns": biome_columns, "surface": surface, "water": water_columns,
        "band": band, "chunks": chunks, "populated": populated,
    }


# Blocks that do not count as "the surface" when measuring column height.
#
# Water is a BLOCK, so without this the highest non-air block over an ocean is the water's own top
# face and every ocean column reports a sea-floor depth of exactly 1. That is not a subtle error --
# it makes an ocean and a rain puddle measure identically, which is precisely the distinction the
# coastline pass has to draw.
WATER_STILL, WATER_FLOWING, ICE, PERMAICE = 271, 270, 730, 731
TRANSPARENT = {WATER_STILL, WATER_FLOWING, ICE, PERMAICE}


def percentile(values, pct):
    if not values:
        return 0
    ordered = sorted(values)
    return ordered[min(len(ordered) - 1, int(len(ordered) * pct / 100.0))]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--blocks", default="")
    ap.add_argument("--sea", type=int, default=128)
    ap.add_argument("--top", type=int, default=40)
    args = ap.parse_args()

    wanted = {int(v) for v in args.blocks.split(",") if v.strip()}
    result = scan(args.world, args.dim, wanted, args.sea)

    print("chunks with sections: %d (%d marked TerrainPopulated)"
          % (result["chunks"], result["populated"]))
    total_columns = sum(result["columns"].values())
    total_water = sum(result["water"].values())
    print("columns: %d, of which %d (%.1f%%) have their surface below y=%d"
          % (total_columns, total_water,
             100.0 * total_water / total_columns if total_columns else 0.0, args.sea))
    print()

    print("%-34s %9s %7s %7s %7s %9s" % ("biome", "columns", "medianY", "p05", "p95", "submerged"))
    for name, count in result["columns"].most_common():
        heights = result["surface"][name]
        print("%-34s %9d %7d %7d %7d %8.1f%%"
              % (str(name)[:34], count, percentile(heights, 50), percentile(heights, 5),
                 percentile(heights, 95),
                 100.0 * result["water"][name] / count if count else 0.0))
    print()

    if wanted:
        print("requested blocks, by biome:")
        for name in result["columns"]:
            hits = {b: result["per_biome"][name][b] for b in wanted
                    if result["per_biome"][name][b]}
            if hits:
                print("  %-32s %s" % (str(name)[:32], hits))
        print()
        print("requested blocks, by 16-block Y band:")
        for bid in sorted(wanted):
            bands = result["band"][bid]
            if bands:
                print("  %d: %s" % (bid, {k * 16: v for k, v in sorted(bands.items())}))
    else:
        # The truncation is NAMED, loudly, because reading "absent from this list" as "absent from
        # the world" is a mistake this project has actually made: a run reported "zero sunflowers in
        # the entire save, in any biome" off a default top-40 census of a world that held 191 of
        # them, and that false zero became a queue item. A rare block is BELOW the fold by
        # construction -- the fold is where every decoration lives -- so the only safe way to ask
        # "is block N present" is --blocks N, which counts it whatever its rank.
        shown = result["census"].most_common(args.top)
        print("block census, top %d of %d distinct ids:" % (len(shown), len(result["census"])))
        for bid, count in shown:
            print("  %6d  %d" % (bid, count))
        hidden = len(result["census"]) - len(shown)
        if hidden:
            hidden_total = sum(result["census"].values()) - sum(c for _, c in shown)
            print()
            print("  ... %d further ids (%d blocks) are BELOW THE FOLD and not shown."
                  % (hidden, hidden_total))
            print("  ABSENCE FROM THIS LIST IS NOT ABSENCE FROM THE WORLD. Every decoration block")
            print("  in a large save ranks below 40th. To ask whether a block generated, name it:")
            print("      python tools/scan_world.py %s --blocks <id>[,<id>...]" % args.world)


if __name__ == "__main__":
    main()
