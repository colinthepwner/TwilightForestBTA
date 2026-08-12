#!/usr/bin/env python3
"""
Measure how deep every lava block sits below its own column's surface.

The block census answers "how much lava" and "roughly what height", and neither settles the
question that matters: does lava ever surface? Y bands are 16 blocks wide, which is wider than the
whole margin being tested, so a band report cannot tell a fixed world from a broken one.

This reports the exact depth distribution and, crucially, the shallowest lava in the world. That
single number is the fix's pass/fail:

    python tools/lava_depth_check.py run/tfverify --dim 0

Expected after ChunkDecoratorTF.undergroundY: every block at depth >= LAVA_SURFACE_MARGIN and at
y >= LAVA_FLOOR, i.e. clear of the bedrock floor and never near daylight.
"""
from __future__ import annotations

import argparse
import collections
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import scan_world as sw

LAVA_IDS = {272, 273}          # fluid.lava.flowing, fluid.lava.still

# ⚠️ NOT where the bedrock is -- where lava must not be. The floor pass in SurfaceGeneratorTF lays
# bedrock at `y <= rand.nextInt(5)`, so actual bedrock reaches y=4 AT MOST. This threshold is set
# above that on purpose: it is the "too deep to be anything but wasted" line, and lava between 5 and
# 8 is not in bedrock but is still sitting on the floor of the world where no player will meet it.
# Reporting it under a "bedrock" label overstated the case in an earlier revision of the docs; the
# label now says what it means.
BEDROCK_TOP = 8


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    args = ap.parse_args()

    region_dir = os.path.join(args.world, "dimensions", str(args.dim), "region")
    if not os.path.isdir(region_dir):
        sys.exit(f"no region directory at {region_dir}")

    depths = collections.Counter()
    heights = collections.Counter()
    total = 0
    shallowest = None
    in_bedrock = 0

    for entry in sorted(os.listdir(region_dir)):
        if not entry.endswith(".mcr"):
            continue
        for chunk in sw.region_chunks(os.path.join(region_dir, entry)):
            level = sw.level_of(chunk)
            sections = level.get("Sections") or []
            if not sections:
                continue

            surface = [0] * 256
            found = []
            for section in sections:
                y_pos = section.get("yPos")
                blocks = section.get("Blocks")
                if y_pos is None or not blocks:
                    continue
                base = y_pos * 16
                for index, block in enumerate(blocks):
                    if block == 0:
                        continue
                    y = base + (index >> 8)
                    col = ((index >> 4) & 15) * 16 + (index & 15)
                    if y > surface[col]:
                        surface[col] = y
                    if block in LAVA_IDS:
                        found.append((col, y))

            for col, y in found:
                total += 1
                heights[y] += 1
                depth = surface[col] - y
                depths[depth] += 1
                if y <= BEDROCK_TOP:
                    in_bedrock += 1
                if shallowest is None or depth < shallowest[0]:
                    shallowest = (depth, y, surface[col])

    if not total:
        print("no lava found")
        return

    print(f"lava blocks            : {total}")
    print(f"heights (y -> count)   : {dict(sorted(heights.items()))}")
    print(f"depth below surface    : {dict(sorted(depths.items())[:15])}")
    print(f"on the floor of the world (y <= {BEDROCK_TOP}; bedrock itself only reaches y=4): {in_bedrock}")
    print(f"SHALLOWEST             : depth {shallowest[0]} (lava y={shallowest[1]}, "
          f"surface y={shallowest[2]})")


if __name__ == "__main__":
    main()
