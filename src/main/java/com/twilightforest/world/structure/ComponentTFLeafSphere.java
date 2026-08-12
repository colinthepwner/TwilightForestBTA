package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFLeafSphere extends StructureComponentTF {

	private final int rad;

	protected ComponentTFLeafSphere(int componentType, int x, int y, int z, int radius) {
		super(componentType);
		this.coordBaseMode = 0;
		this.rad = radius;
		this.boundingBox = new BoundingBox(
			x - radius, y - radius, z - radius,
			x + radius, y + radius, z + radius);
	}

	@Override
	public int featureType() {
		return ComponentTFHollowTreeTrunk.HOLLOW_TREE;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int leaves = TFBlocks.LEAVES_TWILIGHT_OAK.id();

		int sx = this.rad;
		int sy = this.rad;
		int sz = this.rad;

		for (byte dx = 0; dx <= this.rad; dx++) {
			for (byte dy = 0; dy <= this.rad; dy++) {
				for (byte dz = 0; dz <= this.rad; dz++) {
					byte dist;
					if (dx >= dy && dx >= dz) {
						dist = (byte) (dx + (byte) (Math.max((int) dy, (int) dz) * 0.5
							+ Math.min((int) dy, (int) dz) * 0.25));
					} else if (dy >= dx && dy >= dz) {
						dist = (byte) (dy + (byte) (Math.max((int) dx, (int) dz) * 0.5
							+ Math.min((int) dx, (int) dz) * 0.25));
					} else {
						dist = (byte) (dz + (byte) (Math.max((int) dx, (int) dy) * 0.5
							+ Math.min((int) dx, (int) dy) * 0.25));
					}

					if (dist > this.rad) {
						continue;
					}

					placeIfEmpty(world, leaves, sx + dx, sy + dy, sz + dz, clip);
					placeIfEmpty(world, leaves, sx + dx, sy + dy, sz - dz, clip);
					placeIfEmpty(world, leaves, sx - dx, sy + dy, sz + dz, clip);
					placeIfEmpty(world, leaves, sx - dx, sy + dy, sz - dz, clip);
					placeIfEmpty(world, leaves, sx + dx, sy - dy, sz + dz, clip);
					placeIfEmpty(world, leaves, sx + dx, sy - dy, sz - dz, clip);
					placeIfEmpty(world, leaves, sx - dx, sy - dy, sz + dz, clip);
					placeIfEmpty(world, leaves, sx - dx, sy - dy, sz - dz, clip);
				}
			}
		}
		return true;
	}

	private void placeIfEmpty(World world, int blockId, int x, int y, int z, BoundingBox clip) {
		if (getBlockIdAt(world, x, y, z, clip) == 0) {
			placeBlock(world, blockId, 0, x, y, z, clip);
		}
	}
}
