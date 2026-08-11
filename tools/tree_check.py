"""Find trees that are not standing on anything.

Two failures look the same from a distance and have different causes, so this separates them:

  * a trunk whose base sits on AIR   -- the tree was placed above the ground it was meant to root in
  * a trunk whose base sits on WATER -- the tree was placed on a lake surface

Both come out of the same decision: which y a tree is planted at. Counting them is the only way to
tell whether a change to that fixed the problem or moved it.

    python tools/tree_check.py run/tfverify --dim 0
"""

import argparse
import collections
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Twilight Forest's three logs, then vanilla's. A trunk base is the lowest log in a column.
TF_LOGS = {2301, 2302, 2303}
VANILLA_LOGS = {17}
LOGS = TF_LOGS | VANILLA_LOGS

AIR = 0
WATER = {8, 9}
# What a tree may legitimately stand on: dirt, grass, sand, podzol-likes, and its own kind (a
# canopy tree's branches sit on its trunk).
GROUND = {1, 2, 3, 12, 13, 60, 110} | LOGS

# How many stacked logs make a trunk rather than a branch.
MIN_TRUNK = 3


def blocks_of(level):
    """Returns {(x, y, z): id} for one chunk, from BTA's sectioned format."""
    out = {}
    for section in level.get("Sections") or []:
        base = section.get("yPos", 0) * 16
        data = section.get("Blocks")
        if not data:
            continue
        for i, value in enumerate(data):
            if value == AIR:
                continue
            y = base + (i >> 8)
            z = (i >> 4) & 15
            x = i & 15
            # 0xFFFF, not 0xFF. BTA stores block ids as a short array and this mod's own blocks
            # are 2301-2313, so masking to a byte turns every one of them into an unrelated
            # vanilla id -- and the measurement reads as a clean result rather than a broken one.
            out[(x, y, z)] = value & 0xFFFF
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--show", type=int, default=8, help="how many offending positions to print")
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    trunks = 0
    on_air = []
    on_water = []
    footing = collections.Counter()
    chunks = 0

    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            chunks += 1
            level = level_of(chunk)
            blocks = blocks_of(level)
            if not blocks:
                continue
            cx = level.get("xPos", 0) * 16
            cz = level.get("zPos", 0) * 16

            for (x, y, z), block in blocks.items():
                if block not in LOGS:
                    continue
                # ⚠️ A TRUNK is a vertical run of at least MIN_TRUNK logs, not simply the lowest log
                # in a column. Canopy trees are mostly horizontal branches, and every branch log has
                # air or leaves under it -- counting those made a third of the "trees" in this world
                # look like they were floating when they were just limbs.
                if blocks.get((x, y - 1, z)) in LOGS:
                    continue
                run = 0
                while blocks.get((x, y + run, z)) in LOGS:
                    run += 1
                if run < MIN_TRUNK:
                    continue

                trunks += 1
                below = blocks.get((x, y - 1, z), AIR)
                footing[below] += 1
                if below == AIR:
                    on_air.append((cx + x, y, cz + z, block, run))
                elif below in WATER:
                    on_water.append((cx + x, y, cz + z, block, run))

    print("%d chunks, %d tree trunks\n" % (chunks, trunks))
    if not trunks:
        print("NO TREES AT ALL -- that is its own failure.")
        return 1

    print("what trunks are standing on:")
    for block, n in footing.most_common(10):
        label = "air" if block == AIR else ("water" if block in WATER else str(block))
        ok = "" if block == AIR or block in WATER else "  ok"
        print("  %-8s %6d  (%4.1f%%)%s" % (label, n, 100.0 * n / trunks, ok))

    print()
    print("floating (on air):   %d  (%.1f%%)" % (len(on_air), 100.0 * len(on_air) / trunks))
    print("on water:            %d  (%.1f%%)" % (len(on_water), 100.0 * len(on_water) / trunks))

    for label, hits in (("floating", on_air), ("on water", on_water)):
        for pos in hits[: args.show]:
            print("  %s: log %d, %d tall, at x=%d y=%d z=%d" % (label, pos[3], pos[4], pos[0], pos[1], pos[2]))

    return 1 if (on_air or on_water) else 0


if __name__ == "__main__":
    raise SystemExit(main())
