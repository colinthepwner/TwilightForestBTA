"""Census the loot chests a generated world actually contains.

`TFTreasure` having a table is not evidence that any chest holds it. A table can be complete, its
placer can be wired, and the chest can still land empty -- because the fill ran before the tile
entity existed, because the structure clipped it away at a chunk boundary, or because the placer was
never reached. This reads the chests back out of the region files and reports what is in them.

    python tools/chest_check.py run/tfverify --dim 0

An EMPTY chest is the finding worth looking for. A world with no chests at all usually means the
structures that carry them did not generate in this sample -- check structure_check.py first.
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Which structure a chest belongs to, inferred from its height. The bands come from the same source
# the marker table does: hollow-hill chambers sit at SEA_LEVEL-3, the hill maze 20 blocks under
# that, hedge mazes and courtyards on the surface, tower rooms far above it.
def structure_of(y):
    if y <= 20:
        return "hill maze (underhill)"
    if y <= 31:
        return "hollow hill"
    if y <= 45:
        return "surface (hedge maze / hut / courtyard)"
    return "Lich tower"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--verbose", action="store_true", help="list every chest's contents")
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    chests = []
    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            level = level_of(chunk)
            for tile in level.get("TileEntities", []) or []:
                if tile.get("id") not in ("minecraft:chest", "Chest"):
                    continue
                items = tile.get("Items") or []
                chests.append((tile.get("x", 0), tile.get("y", 0), tile.get("z", 0), items))

    if not chests:
        print("no chests found -- check structure_check.py before reading anything into this")
        return 0

    by_structure = collections.Counter()
    empty = collections.Counter()
    stacks = collections.Counter()
    item_ids = collections.Counter()

    for x, y, z, items in chests:
        where = structure_of(y)
        by_structure[where] += 1
        if not items:
            empty[where] += 1
            print("  EMPTY chest at %d,%d,%d (%s)" % (x, y, z, where))
        stacks[where] += len(items)
        for it in items:
            item_ids[it.get("id")] += it.get("Count", 1)
        if args.verbose:
            print("  chest %d,%d,%d (%s): %s" % (x, y, z, where,
                  ", ".join("%sx%s" % (i.get("id"), i.get("Count", 1)) for i in items)))

    print("\n%d chests\n" % len(chests))
    print("%-40s %7s %7s %9s" % ("structure", "chests", "empty", "stacks"))
    for where in sorted(by_structure):
        print("%-40s %7d %7d %9d"
              % (where, by_structure[where], empty[where], stacks[where]))

    print("\ndistinct item types across all chests: %d" % len(item_ids))
    for ident, count in item_ids.most_common(20):
        print("  %-34s %6d" % (ident, count))

    total_empty = sum(empty.values())
    print()
    print("PASS: every chest has loot." if total_empty == 0
          else "FAIL: %d of %d chests are empty." % (total_empty, len(chests)))
    return 0 if total_empty == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
