"""Metadata histogram for chosen block ids, read back out of a generated world.

Answers "did the generator write the data value it thinks it did?", which no other tool here does --
scan_world.py counts BLOCKS and ignores their metadata entirely. That gap matters for anything whose
appearance is driven by data rather than by id: the giant mushroom's face table, a log's axis, a slab's
half.

    python tools/meta_check.py run/tfverify --dim 0 --blocks 2314,2315

⚠️ BTA STORES `Data` AS ONE BYTE PER BLOCK, NOT PACKED NIBBLES.

Minecraft packs two data values into each byte, so every reader ever written for the format does
`data[i >> 1]` and picks a nibble. BTA does not: `len(Data) == len(Blocks) == 4096`, one plain byte
each. This is a fifth deviation on top of the four scan_world.py documents.

It is worth being loud about because of HOW it fails. A nibble decode against byte-per-block data
does not error and does not produce obvious nonsense -- it reads the low half of each byte and
returns 0 for most blocks, so the histogram comes back overwhelmingly metadata 0 with a scatter of
small values. That looks exactly like "the generator never wrote any metadata", which is a bug
report someone will then go and chase through the generator. It cost one such chase to find.

The assertion below fails loudly rather than guessing, so if BTA ever switches to nibbles this
stops instead of silently returning the other wrong answer.
"""

import argparse
import collections
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from scan_world import level_of, region_chunks  # noqa: E402


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("world")
    ap.add_argument("--dim", type=int, default=0)
    ap.add_argument("--blocks", required=True,
                    help="comma-separated block ids, e.g. 2314,2315")
    args = ap.parse_args()

    wanted = {int(v) for v in args.blocks.split(",")}

    region_dir = Path(args.world) / "dimensions" / str(args.dim) / "region"
    if not region_dir.is_dir():
        sys.exit("no region directory at %s" % region_dir)

    counts = collections.Counter()
    for region in sorted(region_dir.glob("*.mcr")):
        for chunk in region_chunks(region):
            level = level_of(chunk)
            for section in level.get("Sections") or []:
                blocks = section.get("Blocks")
                data = section.get("Data")
                if not blocks or data is None:
                    continue
                if len(data) != len(blocks):
                    sys.exit("Data is %d bytes for %d blocks -- this reader assumes ONE BYTE PER "
                             "BLOCK, which is what BTA writes. If that has changed, fix the decode "
                             "rather than the ratio." % (len(data), len(blocks)))
                for i, value in enumerate(blocks):
                    bid = value & 0xFFFF
                    if bid in wanted:
                        counts[(bid, data[i])] += 1

    if not counts:
        print("none of %s found in this world" % sorted(wanted))
        return 0

    print("%-8s %-6s %9s" % ("block", "meta", "count"))
    for (bid, meta), n in sorted(counts.items()):
        print("%-8d %-6d %9d" % (bid, meta, n))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
