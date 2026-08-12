package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofStairs extends ComponentTFTowerRoof {

	public ComponentTFTowerRoofStairs(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = 0;
		this.size = wing.size;
		this.height = this.size / 2;
		makeCapBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return buildHippedRoof(world, clip);
	}

	protected boolean buildHippedRoof(World world, BoundingBox clip) {
		int stairs = ComponentTFTowerWing.STAIRS_WOOD;
		int[] meta = ComponentTFTowerWing.STAIR_META;

		for (int y = 0; y <= this.height; y++) {
			int min = y;
			int max = this.size - y - 1;

			for (int x = y; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					if (x == min) {
						if (z != min && z != max) {
							placeBlock(world, stairs, meta[0], x, y, z, clip);
						} else {
							placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
								ComponentTFTowerWing.SLAB_LOWER, x, y, z, clip);
						}
					} else if (x == max) {
						if (z != min && z != max) {
							placeBlock(world, stairs, meta[1], x, y, z, clip);
						} else {
							placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
								ComponentTFTowerWing.SLAB_LOWER, x, y, z, clip);
						}
					} else if (z == max) {
						placeBlock(world, stairs, meta[3], x, y, z, clip);
					} else if (z == min) {
						placeBlock(world, stairs, meta[2], x, y, z, clip);
					} else {

						placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
							ComponentTFTowerWing.SLAB_DOUBLE, x, y, z, clip);
					}
				}
			}
		}
		return true;
	}
}
