"""Count the marker blocks that prove each big structure generated.

A structure either exists or it does not, and the cheapest proof is a block only that structure
places. Each row below is a block no other feature in the mod uses, so a zero means that structure
did not build -- not that it built badly.

    python tools/structure_check.py run/tfverify --dim 0
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402

# Block id -> (what it proves, roughly how many one instance places)
MARKERS = {
    2310: ("hedge maze walls", "hundreds"),
    2313: ("Naga courtyard boss spawner", "exactly 1 per courtyard"),
    2311: ("fireflies (hedge maze torches + courtyard pillars)", "tens"),
    2307: ("mazestone (hill maze)", "hundreds"),
    2308: ("mazestone cobble (hill maze)", "hundreds"),
    2309: ("mazestone mossy (hill maze)", "hundreds"),
}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    args = ap.parse_args()

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    counts = collections.Counter()
    ys = collections.defaultdict(list)
    chunks = 0

    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            chunks += 1
            level = level_of(chunk)
            for section in level.get("Sections") or []:
                base = section.get("yPos", 0) * 16
                data = section.get("Blocks")
                if not data:
                    continue
                for i, value in enumerate(data):
                    # 0xFFFF: this mod's blocks are 2301-2313 and a byte mask hides all of them.
                    bid = value & 0xFFFF
                    if bid in MARKERS:
                        counts[bid] += 1
                        if len(ys[bid]) < 40000:
                            ys[bid].append(base + (i >> 8))

    print("%d chunks\n" % chunks)
    missing = []
    for bid, (what, scale) in MARKERS.items():
        n = counts.get(bid, 0)
        if n:
            span = "y %d-%d" % (min(ys[bid]), max(ys[bid]))
            print("  %-6d %-46s %7d  %s" % (bid, what, n, span))
        else:
            print("  %-6d %-46s %7d  (expected %s)" % (bid, what, 0, scale))
            missing.append(what)

    print()
    if missing:
        print("NOT FOUND: %s" % "; ".join(missing))
        print("A structure is rare -- widen the sample before calling it broken.")
        return 1
    print("PASS: every marker present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
