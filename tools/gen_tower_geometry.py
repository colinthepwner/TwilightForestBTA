#!/usr/bin/env python3
"""Emit the two Bedrock geometries the dark tower's mobs need and the asset bridge cannot make.

    python tools/gen_tower_geometry.py

Writes src/main/resources/assets/twilightforest/models/entity/towergolem.json and
towertermite.json. Companion to tools/gen_mosquito_geometry.py, and shipped for a related but
distinct reason -- read this before deleting either file.

--- Why these two are not bridged -------------------------------------------------------------

model-bridge.properties converts the ORIGINAL's model classes into Bedrock geometry. That works
whenever the original authored its own boxes. These two mobs did not:

    ModelTFTowerGolem  extends axr (Minecraft's ModelIronGolem) and declares NO boxes at all --
                       it is thirteen lines, all of them animation. towergolem.png is a 128x128
                       IRON GOLEM reskin.
    EntityTFTowerTermite  registers no model class whatsoever. TFClientProxy hands it `new axi()`,
                       which is Minecraft's ModelSilverfish. towertermite.png is a 64x32
                       SILVERFISH reskin -- confirmed by matching the opaque regions of the PNG
                       against ModelSilverfish's own UV table, which lines up on all ten boxes.

The bridge's usual answer to a vanilla base class is base.*.slots: rebuild the base layer from the
Bedrock geometry BTA already ships, then compose the subclass's boxes on top. That answer is
unavailable here, because BTA ships NO iron golem and NO silverfish -- the two mobs do not exist in
this game. There is nothing to compose against and nothing to bridge.

So the boxes are transcribed here from Minecraft 1.2.3's own model classes, read out of
_reference/mc123/client.jar, which this repository already vendors as a decompilation reference:

    ta.java   ModelIronGolem   (head a, body b, rightArm c, leftArm d, rightLeg e, leftLeg f)
    aiq.java  ModelSilverfish  (7 body segments in array a, 3 plates in array b)

1.2.3 rather than 1.4.7 for the same reason model-bridge.properties gives for its own field names:
Minecraft numbers members per class, so a class whose shape has not changed keeps them across
versions, and neither of these two changed between 1.2.3 and 1.4.7. 1.2.3 is the jar this repo has.

--- The conversion, and it is verified ---------------------------------------------------------

Java model space hangs down from a 24-unit origin; Bedrock is Y-up. The rule is TFGeometryBridge's,
quoted from its own class comment:

    pivot  = ( rpX, 24 - rpY, rpZ )
    origin = ( pivotX + offX, pivotY - offY - height, pivotZ + offZ )

Checked against a model BOTH engines have, rather than trusted: Minecraft's ModelSpider converts to
BTA's shipped assets/minecraft/models/entity/spider/spider.geo.json exactly, on all three bones --
head pivot [0,9,-3] origin [-4,5,-11], body0 pivot [0,9,0] origin [-3,6,-3], body1 pivot [0,9,9]
origin [-5,5,3]. If this rule is ever wrong, that check fails first.

z keeps its sign because MobRenderer.preRenderTransform scales z by a NEGATIVE 0.0625 and flips the
axis for the whole model.

--- Registering the output ---------------------------------------------------------------------

Both files must be listed twice:
  * assets/twilightforest/models/entity/models.json      -- the jar's own manifest
  * TFGeometryBridge.BUILTIN_MODEL_IDS                   -- so the GENERATED pack's manifest, which
                                                            shadows the jar's, does not drop them
"""

import json
import os

OUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "resources", "assets", "twilightforest", "models", "entity")

JAVA_ORIGIN_Y = 24.0


def cube(rp, off, size, uv, inflate=0.0):
    """One Java addBox under one setRotationPoint, in Bedrock terms.

    rp   (x, y, z) the Java rotation point
    off  (x, y, z) the Java addBox offset
    size (w, h, d)
    """
    px, py, pz = rp[0], JAVA_ORIGIN_Y - rp[1], rp[2]
    c = {
        "origin": [px + off[0], py - off[1] - size[1], pz + off[2]],
        "size": list(size),
        "uv": list(uv),
    }
    if inflate:
        c["inflate"] = inflate
    return c


def bone(name, rp, cubes, parent=None, mirror=False):
    b = {"name": name, "pivot": [rp[0], JAVA_ORIGIN_Y - rp[1], rp[2]]}
    if parent:
        b["parent"] = parent
    if mirror:
        b["mirror"] = True
    b["cubes"] = cubes
    return b


def write(identifier, tex_w, tex_h, bounds, bones, filename):
    doc = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": identifier,
                "texture_width": tex_w,
                "texture_height": tex_h,
                "visible_bounds_width": bounds[0],
                "visible_bounds_height": bounds[1],
                "visible_bounds_offset": bounds[2],
            },
            "bones": bones,
        }],
    }
    path = os.path.join(OUT_DIR, filename)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(doc, fh, indent=1)
        fh.write("\n")
    print("wrote", path)


# ----------------------------------------------------------------------------------------------
# The tower golem: Minecraft 1.2.3 ta.java, ModelIronGolem, with its constructor's two arguments at
# their defaults (scale 0.0, yOffset -7.0) -- which is how TFClientProxy builds it.
# ----------------------------------------------------------------------------------------------

Y_OFF = -7.0

golem_bones = [
    # head: the skull plus the nose stub. Both hang off one pivot two units back from centre.
    bone("head", (0.0, Y_OFF, -2.0), [
        cube((0.0, Y_OFF, -2.0), (-4.0, -12.0, -5.5), (8, 10, 8), (0, 0)),
        cube((0.0, Y_OFF, -2.0), (-1.0, -5.0, -7.5), (2, 4, 2), (24, 0)),
    ]),
    # body: the torso plus the belt, which is inflated half a unit so it stands proud of the torso
    # rather than z-fighting with it.
    bone("body", (0.0, Y_OFF, 0.0), [
        cube((0.0, Y_OFF, 0.0), (-9.0, -2.0, -6.0), (18, 12, 11), (0, 40)),
        cube((0.0, Y_OFF, 0.0), (-4.5, 10.0, -3.0), (9, 5, 6), (0, 70), inflate=0.5),
    ]),
    # The arms are THIRTY units long and pivot at the shoulder, which is what makes the golem's
    # swing read from across a room. They are separate bones because the attack animation drives
    # them together and the walk drives them not at all.
    bone("rightArm", (0.0, Y_OFF, 0.0), [
        cube((0.0, Y_OFF, 0.0), (-13.0, -2.5, -3.0), (4, 30, 6), (60, 21)),
    ]),
    bone("leftArm", (0.0, Y_OFF, 0.0), [
        cube((0.0, Y_OFF, 0.0), (9.0, -2.5, -3.0), (4, 30, 6), (60, 58)),
    ]),
    bone("rightLeg", (-4.0, 18.0 + Y_OFF, 0.0), [
        cube((-4.0, 18.0 + Y_OFF, 0.0), (-3.5, -3.0, -3.0), (6, 16, 5), (37, 0)),
    ]),
    # ⚠️ The left leg is MIRRORED in the original (`this.f.i = true`), and it has its own texture
    # offset rather than reusing the right leg's -- so the mirror is about which way the seam runs,
    # not about saving atlas space.
    bone("leftLeg", (5.0, 18.0 + Y_OFF, 0.0), [
        cube((5.0, 18.0 + Y_OFF, 0.0), (-3.5, -3.0, -3.0), (6, 16, 5), (60, 0)),
    ], mirror=True),
]

# 43 units tall, feet at 0: 2.7 blocks against the mob's 2.8-block hitbox, which is the check that
# the conversion landed the right way up.
write("geometry.towergolem", 128, 128, (2.5, 2.8, [0, 1.4, 0]), golem_bones, "towergolem.json")


# ----------------------------------------------------------------------------------------------
# The tower termite: Minecraft 1.2.3 aiq.java, ModelSilverfish.
#
# Seven body segments laid nose to tail along z, each sized and UV'd from the class's own two
# tables, and each with its own pivot so the whole thing can squirm. Then three flat plates over
# the top of segments 1, 2 and 4.
# ----------------------------------------------------------------------------------------------

SEG_SIZE = [(3, 2, 2), (4, 3, 2), (6, 4, 3), (3, 3, 3), (2, 2, 3), (2, 1, 2), (1, 1, 2)]
SEG_UV = [(0, 0), (0, 4), (0, 9), (0, 16), (0, 22), (11, 0), (13, 4)]

# The original walks z forward as it lays the segments down, each step half of this segment's depth
# plus half of the next one's -- so consecutive segments touch exactly, whatever their sizes.
seg_z = []
z = -3.5
for i in range(7):
    seg_z.append(z)
    if i < 6:
        z += (SEG_SIZE[i][2] + SEG_SIZE[i + 1][2]) * 0.5

termite_bones = []
for i, (size, uv) in enumerate(zip(SEG_SIZE, SEG_UV)):
    # setRotationPoint(0, 24 - height, z): every segment's pivot sits on TOP of it, so rotating one
    # swings it sideways about its own spine rather than lifting it.
    rp = (0.0, JAVA_ORIGIN_Y - size[1], seg_z[i])
    off = (size[0] * -0.5, 0.0, size[2] * -0.5)
    termite_bones.append(bone("segment%d" % i, rp, [cube(rp, off, size, uv)]))

# ⚠️ The three plates are irregular on purpose and one of them is an upstream slip: plate 2 takes
# its z OFFSET from segment 4's depth but its own DEPTH from segment 1's. Transcribed as written --
# it is what the art is painted for.
termite_bones.append(bone("plate0", (0.0, 16.0, seg_z[2]), [
    cube((0.0, 16.0, seg_z[2]), (-5.0, 0.0, SEG_SIZE[2][2] * -0.5), (10, 8, SEG_SIZE[2][2]), (20, 0)),
]))
termite_bones.append(bone("plate1", (0.0, 20.0, seg_z[4]), [
    cube((0.0, 20.0, seg_z[4]), (-3.0, 0.0, SEG_SIZE[4][2] * -0.5), (6, 4, SEG_SIZE[4][2]), (20, 11)),
]))
termite_bones.append(bone("plate2", (0.0, 19.0, seg_z[1]), [
    cube((0.0, 19.0, seg_z[1]), (-3.0, 0.0, SEG_SIZE[4][2] * -0.5), (6, 5, SEG_SIZE[1][2]), (20, 18)),
]))

write("geometry.towertermite", 64, 32, (1.0, 0.75, [0, 0.25, 0]), termite_bones, "towertermite.json")
