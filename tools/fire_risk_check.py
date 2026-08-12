"""Count flammable blocks that lava can actually set alight.

Answers the report "the structures catch fire". Fire itself is a terrible thing to census -- it is
transient, it only ticks in loaded chunks, and a world scanned after the fact shows the CHARCOAL of
a fire that has already gone out, not the fire. So this measures the *precondition* instead, which
is static and therefore honest: a flammable block within reach of lava.

Vanilla ignition is the 3x3x3 neighbourhood plus the block above a lava column, so 26-connectivity
is the right test and a hit means the game can start a fire there, not that it necessarily has.

    python tools/fire_risk_check.py run/tfverify --dim 0
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# BTA ids. The mod's OWN logs and leaves (2301-2306) are deliberately absent: TFBlocks tags them
# MINEABLE_BY_AXE/SHEARS and nothing else, so they carry no flammability and cannot be the ignition
# point. Everything below is BTA's own, and BTA's own blocks burn.
FLAMMABLE = {
    280: "oak log", 281: "pine log", 282: "birch log", 283: "cherry log",
    284: "eucalyptus log", 285: "mossy oak log", 286: "thorn log", 287: "palm log",
    290: "oak leaves", 291: "pine leaves", 292: "birch leaves",
    17: "planks", 80: "oak fence", 70: "ladder",
    35: "wool",
}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--lava", default="272,273",
                    help="lava block ids (still,flowing) -- BTA renumbered, so pass them explicitly")
    args = ap.parse_args()

    lava_ids = {int(v) for v in args.lava.split(",")}

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    world = {}
    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            level = level_of(chunk)
            cx, cz = level.get("xPos", 0), level.get("zPos", 0)
            for section in level.get("Sections") or []:
                base = section.get("yPos", 0) * 16
                data = section.get("Blocks")
                if not data:
                    continue
                for i, value in enumerate(data):
                    bid = value & 0xFFFF
                    if bid == 0:
                        continue
                    y = base + (i >> 8)
                    z = (i >> 4) & 15
                    x = i & 15
                    world[(cx * 16 + x, y, cz * 16 + z)] = bid

    lava = {p for p, b in world.items() if b in lava_ids}
    print("%d blocks loaded, %d lava" % (len(world), len(lava)))
    if not lava:
        print("no lava with ids %s -- pass --lava with the right ids" % sorted(lava_ids))
        return 0

    offsets = [(dx, dy, dz)
               for dx in (-1, 0, 1) for dy in (-1, 0, 1) for dz in (-1, 0, 1)
               if (dx, dy, dz) != (0, 0, 0)]

    at_risk = collections.Counter()
    ys = []
    for (x, y, z) in lava:
        for dx, dy, dz in offsets:
            neighbour = world.get((x + dx, y + dy, z + dz))
            if neighbour in FLAMMABLE:
                at_risk[FLAMMABLE[neighbour]] += 1
                ys.append(y + dy)

    total = sum(at_risk.values())
    print("\nflammable blocks touching lava: %d" % total)
    for name, n in at_risk.most_common():
        print("  %-16s %6d" % (name, n))
    if ys:
        print("\nY range of contacts: %d to %d" % (min(ys), max(ys)))
    print()
    print("PASS: lava touches nothing that burns." if total == 0
          else "FAIL: %d ignition points. Every one is a fire the game can start." % total)
    return 0 if total == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
