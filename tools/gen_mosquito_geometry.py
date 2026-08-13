"""Generate the mosquito swarm's Bedrock geometry.

    python tools/gen_mosquito_geometry.py

Writes src/main/resources/assets/twilightforest/models/entity/mosquitoswarm.json.

⚠️ THIS IS THE ONE ENTITY GEOMETRY THIS MOD SHIPS, AND IT IS NOT THE ORIGINAL'S.

Every other mob's boxes are converted out of the player's own copy of Twilight Forest by
TFGeometryBridge, which is both the only way to get them and the only way to keep Benimatic's work
out of this repository. The mosquito swarm is the one model that cannot come through that path, and
the reason is worth stating precisely because it is not "the bridge is not clever enough":

    public void addBugsToNodes(ayf node) {
        int bugs = 16;
        for (int i = 0; i < bugs; ++i) {
            aoj vec = aoj.a(11.0, 0.0, 0.0);
            float rotateY = (float) i * (360.0f / (float) bugs) * 3.141593f / 180.0f;
            vec.b(rotateY);
            ayf bug = new ayf((axa) this, this.rand.nextInt(28), this.rand.nextInt(28));
            float bugX = (this.rand.nextFloat() - this.rand.nextFloat()) * 4.0f;
            ...
            node.a(bug);
        }
    }

ModelTFMosquitoSwarm's constructor declares seven boxes -- a core and six nodes, every one of them
1x1x1 -- and calls that helper six times to build the other ninety-six. So 96 of the mob's 103 boxes
are made by a LOOP IN A SECOND METHOD, from an UNSEEDED Random. The bridge reads constructors; it
does not unroll loops, and if it did it could not reproduce these positions, because upstream itself
does not: a mosquito swarm's cloud is different every time the model is constructed. There are no
authored coordinates for the 96 to copy.

What upstream *does* author is the arrangement -- six nodes, sixteen bugs each on a ring, all of them
single pixels -- and that is a description rather than a drawing. This file follows the description
and derives every number from this port's own mob instead of from the archive, so nothing here is a
transcription of anything:

  * MobTFMosquitoSwarm is setSize(0.7F, 1.9F) and TFEntityRenderers gives it scale 0.5, so a model
    unit is 1/16 of a block BEFORE that scale -- the hitbox is 0.7*16/0.5 = 22.4 wide and
    1.9*16/0.5 = 60.8 tall in model space. Hence CENTRE_Y, which is half of 60.8 rounded.
  * The cloud comes out 25 units across -- 0.78 blocks at scale 0.5 -- so it is a shade WIDER than the
    0.7 hitbox and a good deal SHORTER than the 1.9. Both are deliberate and both match upstream in
    character: a swarm whose edge you can stand in without being bitten reads as a cloud, one clipped
    to its collision box reads as a solid object, and a 1.9-block column of gnats reads as neither.
  * The wobble is a hash, not a random number, so this script is reproducible: run it twice and the
    file does not move. That is the one place this differs from upstream in kind rather than in
    number, and it has to -- a shipped file cannot re-roll itself per instance.

⚠️ THE BONE NAMES ARE LOAD-BEARING. TFHostileRenderers.Swarm poses `core` and `node1`..`node6` by
name every frame and silently does nothing for a name it cannot find, so renaming a bone here does
not move it -- it freezes it.

⚠️ The bugs are CUBES on the node bones, not bones of their own. Upstream makes each one a
ModelRenderer because that is the only way to give it a position at all, then sets its rotation once
and never touches it again; a 1x1x1 cube with a fixed Y rotation is indistinguishable from one
without. As cubes they inherit their node's tumble exactly as upstream's children inherit it, and the
model carries 7 bones instead of 103.
"""

import io
import json
import math
import os

# --- Derived from the mob, not from the archive. See the module comment. --------------------------

# Half of 1.9 blocks at render scale 0.5, in model units: 1.9 * 16 / 0.5 / 2 = 30.4.
CENTRE_Y = 30.0

# Distance from the cloud's centre to each node. Six nodes on the vertices of an octahedron, which is
# the cheapest even spread over a sphere and needs no table.
NODE_RADIUS = 7.0

# Ring radius for a node's own bugs.
#
# ⚠️ These two do NOT add up. A node's ring lies in the plane PERPENDICULAR to that node's axis, so a
# bug sits sqrt(NODE_RADIUS^2 + RING_RADIUS^2) = 13.9 units from the centre, not 19 -- the six rings
# fall on a common sphere rather than reaching outward from their nodes. That is the point of turning
# them: it is what makes the cloud a shell instead of six discs. Measured on the generated file, the
# cloud spans about 25 units, which at scale 0.5 is 0.78 blocks -- a shade wider than the 0.7 hitbox.
RING_RADIUS = 12.0

BUGS_PER_NODE = 16

# The six octahedron directions, in the order the renderer poses them.
NODES = [
    (1.0, 0.0, 0.0),
    (-1.0, 0.0, 0.0),
    (0.0, 1.0, 0.0),
    (0.0, -1.0, 0.0),
    (0.0, 0.0, 1.0),
    (0.0, 0.0, -1.0),
]

# mosquitoswarm.png is 32x32 and is a plain light-to-dark GRADIENT -- decoded and checked, there is no
# insect drawn on it anywhere. That is why upstream picks `rand.nextInt(28)` for both coordinates: it
# is not aiming at a sprite, it is drawing a shade. A 1x1x1 cube occupies 4x2 texels, so a UV has to
# stay inside 28x30 to be on the sheet at all, and walking the square is what gives the cloud the same
# tonal variation upstream gets from the draw.
TEXTURE = 32
UV_LIMIT = 28

# 13 is coprime to 28*28, so stepping an index by it visits 784 distinct cells before repeating and
# all 103 bugs land on a different one. Advancing u and v separately does NOT do this -- (5n mod 28,
# 3n mod 28) repeats every 28 bugs, so the first version of this file drew the same 28 shades four
# times over.
UV_STRIDE = 13


def wobble(node, index, axis):
    """A small repeatable offset in [-2, 2], standing in for upstream's per-bug jitter.

    Upstream shifts each bug by `(nextFloat() - nextFloat()) * 4`. Two uniforms subtracted is a
    triangular distribution about zero -- most bugs barely move, a few move the full amount -- so this
    mixes the three inputs and then folds the result the same way rather than taking one modulus,
    which would spread them evenly and give the ring a fuzzy edge instead of a ragged one.
    """
    h = (node * 73856093) ^ (index * 19349663) ^ (axis * 83492791)
    h &= 0xFFFFFFFF
    h ^= (h >> 13)
    h = (h * 1274126177) & 0xFFFFFFFF
    h ^= (h >> 16)
    # Two independent 8-bit draws, subtracted, matching the shape of upstream's two nextFloat calls.
    a = (h & 0xFF) / 255.0
    b = ((h >> 8) & 0xFF) / 255.0
    return round((a - b) * 2.0, 2)


def basis(direction):
    """Two unit vectors spanning the plane perpendicular to `direction`.

    A node's bugs ring its own axis rather than all six ringing the Y axis as upstream's do. Upstream
    can get away with six coplanar rings because it then jitters every bug by up to four units in
    three axes; here the rings are what give the cloud its depth, so they are turned to face outwards.
    """
    # Pick whichever world axis is least parallel to `direction`, so the cross product is well formed.
    helper = (0.0, 0.0, 1.0) if abs(direction[1]) < 0.9 else (1.0, 0.0, 0.0)
    u = cross(direction, helper)
    u = normalise(u)
    v = normalise(cross(direction, u))
    return u, v


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0])


def normalise(v):
    length = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / length for c in v)


def uv(step):
    """A distinct cell of the gradient per bug. See UV_STRIDE."""
    index = (step * UV_STRIDE) % (UV_LIMIT * UV_LIMIT)
    return index % UV_LIMIT, index // UV_LIMIT


def speck(cx, cy, cz, u, v):
    """One 1x1x1 bug. Bedrock `origin` is the minimum corner, so centre it by hand."""
    return {
        "origin": [round(cx - 0.5, 2), round(cy - 0.5, 2), round(cz - 0.5, 2)],
        "size": [1, 1, 1],
        "uv": [u, v],
    }


def build():
    bones = []

    # The core carries one speck and is the parent every node hangs off, so the whole cloud turns
    # with it -- which is what the renderer's `core` rotation is for.
    bones.append({
        "name": "core",
        "pivot": [0, CENTRE_Y, 0],
        "cubes": [speck(0.0, CENTRE_Y, 0.0, *uv(0))],
    })

    uv_step = 1
    for n, direction in enumerate(NODES):
        nx = direction[0] * NODE_RADIUS
        ny = CENTRE_Y + direction[1] * NODE_RADIUS
        nz = direction[2] * NODE_RADIUS

        # The node's own speck sits at its pivot, so a node reads as the centre of its own knot.
        cubes = [speck(nx, ny, nz, *uv(uv_step))]
        uv_step += 1

        u_axis, v_axis = basis(direction)
        for i in range(BUGS_PER_NODE):
            angle = 2.0 * math.pi * i / BUGS_PER_NODE
            ring_x = (u_axis[0] * math.cos(angle) + v_axis[0] * math.sin(angle)) * RING_RADIUS
            ring_y = (u_axis[1] * math.cos(angle) + v_axis[1] * math.sin(angle)) * RING_RADIUS
            ring_z = (u_axis[2] * math.cos(angle) + v_axis[2] * math.sin(angle)) * RING_RADIUS

            cx = nx + ring_x + wobble(n, i, 0)
            cy = ny + ring_y + wobble(n, i, 1)
            cz = nz + ring_z + wobble(n, i, 2)

            # Walk the sheet rather than repeat: neighbouring bugs land on different shades of the
            # gradient, which is the whole visual effect upstream's random UV buys.
            cubes.append(speck(cx, cy, cz, *uv(uv_step)))
            uv_step += 1

        bones.append({
            "name": "node%d" % (n + 1),
            "parent": "core",
            "pivot": [round(nx, 2), round(ny, 2), round(nz, 2)],
            "cubes": cubes,
        })

    # visible_bounds the same way TFGeometryBridge computes it, so a shipped model and a converted one
    # describe themselves identically -- a mismatch here shows up as a mob that vanishes at the screen
    # edge rather than as anything obviously wrong.
    reach = 0.0
    min_y = float("inf")
    max_y = float("-inf")
    for bone in bones:
        for cube in bone["cubes"]:
            ox, oy, oz = cube["origin"]
            w, h, d = cube["size"]
            reach = max(reach, abs(ox), abs(ox + w), abs(oz), abs(oz + d))
            min_y = min(min_y, oy)
            max_y = max(max_y, oy + h)

    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.mosquitoswarm",
                    "texture_width": TEXTURE,
                    "texture_height": TEXTURE,
                    "visible_bounds_width": round(reach * 2 / 16.0, 5),
                    "visible_bounds_height": round((max_y - min_y) / 16.0, 5),
                    "visible_bounds_offset": [0, round((max_y + min_y) / 2 / 16.0, 5), 0],
                },
                "bones": bones,
            }
        ],
    }


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    out = os.path.join(root, "src", "main", "resources", "assets", "twilightforest",
                       "models", "entity", "mosquitoswarm.json")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    geometry = build()
    with io.open(out, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(geometry, fh, indent="\t")
        fh.write("\n")

    bones = geometry["minecraft:geometry"][0]["bones"]
    cubes = sum(len(b["cubes"]) for b in bones)
    print("%s\n  %d bones, %d cubes (upstream declares 7 and builds 103)" % (out, len(bones), cubes))


if __name__ == "__main__":
    main()
