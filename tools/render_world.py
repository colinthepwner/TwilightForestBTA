#!/usr/bin/env python3
"""
Render a generated Twilight Forest world to PNGs, straight off the region files.

The point is to *see* the dimension without a client. Three views, each answering a question the
block census cannot:

  biomes.png   -- where the seven biomes actually land, and whether they interlock or band
  height.png   -- the terrain squish as a picture: how low and how flat the world really is
  slice.png    -- a vertical cross-section, which is the only way to see a hollow hill's chamber,
                  its glowstone floor and the stalactites hanging in it

Reuses scan_world.py's NBT reader, which already handles BTA's four McRegion deviations (short
arrays are little-endian, tag 11 is short[], tag 12 is double[], sections key on "yPos").

Writes plain PNGs with zlib and no third-party dependency, because the alternative is asking anyone
who runs this to install Pillow first.

    python tools/render_world.py run/tfverify --dim 0 --out docs/shots
"""
from __future__ import annotations

import argparse
import os
import struct
import sys
import zlib

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import scan_world as sw


# Biome colours are the debug colours Benimatic set with setColor(), so the map reads the way the
# original's own debug view did rather than in colours picked here.
BIOME_COLOURS = {
    "twilightforest:twilight_forest":    (0x00, 0x77, 0x00),
    "twilightforest:twilight_clearings": (0x99, 0xCC, 0x99),
    "twilightforest:twilight_highland":  (0x66, 0x66, 0x66),
    "twilightforest:twilight_mushroom":  (0x99, 0x66, 0x33),
    "twilightforest:twilight_swamp":     (0x99, 0x99, 0x33),
    "twilightforest:twilight_snow":      (0xCC, 0xFF, 0xFF),
    "twilightforest:twilight_glacier":   (0xEE, 0xEE, 0xEE),
}

# Blocks worth colouring distinctly in the cross-section. Everything else falls back to grey scale
# by whether it is solid, so the structures stand out against the terrain.
SLICE_COLOURS = {
    0:   (0x0E, 0x10, 0x18),   # air
    1:   (0x6E, 0x6E, 0x6E),   # stone
    200: (0x3C, 0x8C, 0x3C),   # grass
    220: (0x6B, 0x4A, 0x2F),   # dirt
    251: (0x8A, 0x8A, 0x8A),   # gravel
    260: (0x22, 0x22, 0x22),   # bedrock
    180: (0x3B, 0x2B, 0x4A),   # obsidian
    820: (0xFF, 0xE9, 0x7A),   # glowstone
    730: (0x9F, 0xD8, 0xF0),   # ice
    280: (0x5A, 0x40, 0x25),   # log oak
    281: (0x4A, 0x33, 0x1E),   # log pine
    282: (0xC9, 0xBE, 0xA4),   # log birch
    290: (0x2F, 0x6E, 0x2F),   # leaves oak
    292: (0x25, 0x55, 0x35),   # leaves pine
    293: (0x4E, 0x7E, 0x3A),   # leaves birch
    70:  (0xB8, 0x8B, 0x4A),   # ladder  -- hollow tree shaft
    60:  (0xFF, 0xC0, 0x40),   # torch   -- fireflies
    640: (0x20, 0x50, 0x60),   # spawner
    433: (0x2B, 0x4B, 0xC8),   # lapis block -- monolith crown
    410: (0x5D, 0xE0, 0xD0),   # diamond ore
    390: (0xC0, 0x30, 0x30),   # redstone ore
    370: (0xE0, 0xC0, 0x40),   # gold ore
    360: (0xC8, 0xA0, 0x80),   # iron ore
    350: (0x30, 0x30, 0x30),   # coal ore
    380: (0x30, 0x50, 0xB0),   # lapis ore
    10:  (0xD0, 0x60, 0x20),   # lava
    11:  (0xD0, 0x60, 0x20),
    8:   (0x30, 0x60, 0xC0),   # water
    9:   (0x30, 0x60, 0xC0),
}


def write_png(path, width, height, rows):
    """Minimal truecolour PNG writer. `rows` is a list of bytearrays, 3 bytes per pixel."""
    raw = bytearray()
    for row in rows:
        raw.append(0)          # filter type 0 (None) per scanline
        raw.extend(row)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")

    os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)
    print(f"wrote {path} ({width}x{height})")


def load_chunks(world_dir, dim):
    """({(cx, cz): level-nbt}, {numeric biome id: key}).

    Chunk coordinates come from the chunk's own xPos/zPos -- region_chunks yields the NBT and
    nothing else, so the region filename is not the source of truth here.
    """
    region_dir = os.path.join(world_dir, "dimensions", str(dim), "region")
    if not os.path.isdir(region_dir):
        region_dir = os.path.join(world_dir, "region")
    if not os.path.isdir(region_dir):
        sys.exit(f"no region directory under {world_dir}")

    out = {}
    names = {}
    for name in sorted(os.listdir(region_dir)):
        if not name.endswith(".mcr"):
            continue
        for chunk in sw.region_chunks(os.path.join(region_dir, name)):
            level = sw.level_of(chunk)
            if not level or not level.get("Sections"):
                continue
            if not names:
                names = sw.biome_names(chunk)
            cx = level.get("xPos")
            cz = level.get("zPos")
            if cx is None or cz is None:
                continue
            out[(cx, cz)] = level
    return out, names


def column_biomes(level, names):
    """[biome key] per column, indexed z*16 + x. Plane 0 of any section's BiomeMap."""
    for section in level.get("Sections", []):
        bm = section.get("BiomeMap")
        if bm and len(bm) >= 256:
            return [names.get(bm[i] & 0xFF, bm[i] & 0xFF) for i in range(256)]
    return [None] * 256


def column_blocks(level):
    """{(x, y, z): id} for one chunk, x/z 0..15."""
    blocks = {}
    for section in level.get("Sections", []):
        base = section.get("yPos", 0) * 16
        data = section.get("Blocks")
        if not data:
            continue
        for i, bid in enumerate(data):
            if bid == 0:
                continue
            y = base + (i >> 8)
            z = (i >> 4) & 15
            x = i & 15
            blocks[(x, y, z)] = bid
    return blocks


def render_maps(chunks, names, out_dir):
    xs = [c[0] for c in chunks]
    zs = [c[1] for c in chunks]
    x0, x1 = min(xs), max(xs)
    z0, z1 = min(zs), max(zs)
    w = (x1 - x0 + 1) * 16
    h = (z1 - z0 + 1) * 16

    biome_rows = [bytearray(b"\x00" * (w * 3)) for _ in range(h)]
    height_rows = [bytearray(b"\x00" * (w * 3)) for _ in range(h)]

    surfaces = []
    for (cx, cz), level in chunks.items():
        biomes = column_biomes(level, names)
        blocks = column_blocks(level)

        # Highest non-air per column, computed rather than trusted: HeightMap is BTA's short[] and
        # means "first block that stops light", which is not the same as the surface.
        highest = [0] * 256
        for (lx, y, lz), _ in blocks.items():
            idx = lz * 16 + lx
            if y > highest[idx]:
                highest[idx] = y

        for lz in range(16):
            for lx in range(16):
                px = (cx - x0) * 16 + lx
                pz = (cz - z0) * 16 + lz
                idx = lz * 16 + lx

                r, g, b = BIOME_COLOURS.get(biomes[idx], (0x10, 0x10, 0x10))
                o = px * 3
                biome_rows[pz][o:o + 3] = bytes((r, g, b))

                y = highest[idx]
                surfaces.append(y)
                # 0..96 mapped onto a cool-to-warm ramp; the squish keeps almost everything low, so
                # a linear ramp over the full 0..255 column would render the whole world one colour.
                t = max(0.0, min(1.0, y / 96.0))
                hr = int(40 + 215 * t)
                hg = int(60 + 120 * (1.0 - abs(t - 0.5) * 2))
                hb = int(200 - 170 * t)
                height_rows[pz][o:o + 3] = bytes((hr, hg, hb))

    write_png(os.path.join(out_dir, "biomes.png"), w, h, biome_rows)
    write_png(os.path.join(out_dir, "height.png"), w, h, height_rows)

    if surfaces:
        surfaces.sort()
        print(f"  surface Y: min {surfaces[0]}, median {surfaces[len(surfaces)//2]}, max {surfaces[-1]}")


def render_slice(chunks, out_dir, want_hill, max_y=128, scale=3):
    """A west-to-east vertical cross-section, preferring a strip that contains a hollow hill."""
    by_z = {}
    for (cx, cz) in chunks:
        by_z.setdefault(cz, []).append(cx)

    best = None
    for cz, cxs in sorted(by_z.items()):
        if len(cxs) < 8:
            continue
        run = sorted(cxs)
        # Score a strip by how much glowstone it holds above the floor band -- that is a hollow
        # hill's ceiling and its stalactites, and it is what makes a slice worth looking at.
        score = 0
        for cx in run:
            for (x, y, z), bid in column_blocks(chunks[(cx, cz)]).items():
                if bid == 820 and y > 9:
                    score += 1
        if best is None or score > best[0]:
            best = (score, cz, run)

    if best is None:
        print("  no strip long enough to slice")
        return

    score, cz, run = best
    print(f"  slicing chunk row z={cz} ({len(run)} chunks, glowstone-above-floor score {score})")

    w = len(run) * 16
    rows = [bytearray(b"\x00" * (w * 3)) for _ in range(max_y)]

    for ci, cx in enumerate(sorted(run)):
        blocks = column_blocks(chunks[(cx, cz)])
        for lx in range(16):
            for y in range(max_y):
                bid = blocks.get((lx, y, 8), 0)   # mid-chunk z, so the slice is a clean plane
                colour = SLICE_COLOURS.get(bid)
                if colour is None:
                    colour = (0x50, 0x50, 0x50) if bid else SLICE_COLOURS[0]
                px = ci * 16 + lx
                # PNG row 0 is the top, so invert Y to draw the world the right way up.
                o = px * 3
                rows[max_y - 1 - y][o:o + 3] = bytes(colour)

    if scale > 1:
        big = []
        for row in rows:
            wide = bytearray()
            for px in range(w):
                wide.extend(row[px * 3:px * 3 + 3] * scale)
            for _ in range(scale):
                big.append(bytearray(wide))
        write_png(os.path.join(out_dir, "slice.png"), w * scale, max_y * scale, big)
    else:
        write_png(os.path.join(out_dir, "slice.png"), w, max_y, rows)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--out", default="docs/shots")
    ap.add_argument("--hill", action="store_true", help="prefer a slice containing a hollow hill")
    args = ap.parse_args()

    chunks, names = load_chunks(args.world, args.dim)
    print(f"loaded {len(chunks)} chunks with block data, {len(names)} biomes in the registry")
    if not chunks:
        sys.exit("nothing to render")

    render_maps(chunks, names, args.out)
    render_slice(chunks, args.out, args.hill)


if __name__ == "__main__":
    main()
