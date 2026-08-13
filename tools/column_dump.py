"""Print the blocks around one coordinate, so a reported defect can be looked at directly.

tree_check.py names the lowest log of every floating tree it finds. That name is only useful if
something can then go and read the column, which is what this does:

    python tools/column_dump.py run/tfverify --dim 0 --at -137,38,-18

Prints the column itself over a Y window, then the same window for the 3x3 of columns around it, so
"the tree is standing on air" can be checked against what is actually underneath and beside it.

⚠️ Reads the SAME five McRegion deviations scan_world.py documents, by importing its reader rather
than reimplementing it -- in particular `Data` is one byte per block, not packed nibbles.
"""

import argparse
import glob
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Only the ids this port cares about get a name; everything else prints as a bare number, which is
# honest about the fact that BTA's block table is not transcribed here.
NAMES = {
    # ⚠️ BTA ids, not vanilla's, and 200/220 are the pair that matters most here. They are
    # tree_check.py's, read off Blocks.class -- and they are the way round that looks wrong:
    # GRASS is 200 and DIRT is 220. Confirmed independently in this world rather than taken on
    # trust: vanillaPass scatters underground blobs of Blocks.DIRT, and those blobs read 220.
    0: "air", 1: "stone", 200: "grass", 220: "dirt", 221: "dirt.scorched",
    251: "gravel", 260: "bedrock", 270: "water", 271: "water(still)",
    280: "log.oak", 281: "log.pine", 282: "log.birch",
    70: "ladder",
    280: "log.oak", 281: "log.pine", 282: "log.birch",
    2301: "log.twilight_oak", 2302: "log.canopy", 2303: "log.mangrove",
    2304: "leaves.twilight_oak", 2305: "leaves.canopy", 2306: "leaves.mangrove",
    2307: "mazestone", 2309: "mazestone.mossy", 2310: "hedge", 2311: "firefly",
    2312: "cicada", 2313: "boss_spawner",
    2314: "mushroom_giant.brown", 2315: "mushroom_giant.red",
    2316: "log.darkwood", 2317: "leaves.darkwood", 2318: "leaves.rainbow", 2319: "roots",
}


def region_files(world_dir, dim):
    region = os.path.join(world_dir, "dimensions", str(dim), "region")
    if not os.path.isdir(region):
        region = os.path.join(world_dir, "region")
    return sorted(glob.glob(os.path.join(region, "*.mcr")))


def load_area(world_dir, dim, cx0, cz0, cx1, cz1):
    """{(x, y, z): id} for the chunks covering a small box. One dict, so columns can be crossed.

    Only the region files whose 32x32 chunk block overlaps the box are opened -- a floating tree is
    one column and a world is hundreds of megabytes.
    """
    blocks = {}
    for path in region_files(world_dir, dim):
        rx, rz = (int(v) for v in os.path.basename(path).split(".")[1:3])
        if not (rx * 32 <= cx1 and (rx + 1) * 32 > cx0
                and rz * 32 <= cz1 and (rz + 1) * 32 > cz0):
            continue
        for chunk in region_chunks(path):
            level = level_of(chunk)
            cx, cz = level.get("xPos"), level.get("zPos")
            if cx is None or not (cx0 <= cx <= cx1 and cz0 <= cz <= cz1):
                continue
            for section in level.get("Sections") or []:
                ids = section.get("Blocks")
                if not ids:
                    continue
                base = (section.get("yPos") or 0) * 16
                for i, bid in enumerate(ids):
                    if not bid:
                        continue
                    # McRegion section order is y, z, x -- the order scan_world.py walks.
                    y = base + (i >> 8)
                    z = (i >> 4) & 15
                    x = i & 15
                    blocks[(cx * 16 + x, y, cz * 16 + z)] = bid & 0xFFFF
    return blocks


def label(bid):
    return "%s(%d)" % (NAMES[bid], bid) if bid in NAMES else str(bid)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--at", required=True, help="x,y,z")
    ap.add_argument("--span", type=int, default=14, help="Y blocks above and below")
    ap.add_argument("--radius", type=int, default=1, help="columns either side for the grid")
    args = ap.parse_args()

    x, y, z = (int(v) for v in args.at.split(","))
    r = args.radius
    blocks = load_area(args.world, args.dim,
                       (x - r - 1) // 16, (z - r - 1) // 16,
                       (x + r + 1) // 16, (z + r + 1) // 16)
    if not blocks:
        raise SystemExit("no chunks loaded around %d,%d -- outside the generated area?" % (x, z))

    lo, hi = y - args.span, y + args.span

    print("column x=%d z=%d, y %d..%d" % (x, z, lo, hi))
    for yy in range(hi, lo - 1, -1):
        mark = " <== reported here" if yy == y else ""
        print("  y=%-4d %s%s" % (yy, label(blocks.get((x, yy, z), 0)), mark))

    print()
    print("the %dx%d around it (rows are z, columns are x):" % (2 * r + 1, 2 * r + 1))
    header = "  y     " + "".join("x=%-14d" % (x + dx) for dx in range(-r, r + 1))
    for zz in range(z - r, z + r + 1):
        print("z=%d" % zz)
        print(header)
        for yy in range(hi, lo - 1, -1):
            row = "".join("%-16s" % label(blocks.get((x + dx, yy, zz), 0))
                          for dx in range(-r, r + 1))
            print("  %-6d%s" % (yy, row))
        print()


if __name__ == "__main__":
    main()
