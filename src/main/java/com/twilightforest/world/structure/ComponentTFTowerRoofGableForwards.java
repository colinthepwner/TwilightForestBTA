package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofGableForwards extends ComponentTFTowerRoof {

	public ComponentTFTowerRoofGableForwards(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size + 2;
		this.height = this.size;
		makeAttachedOverhangBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int slabId = ComponentTFTowerWing.SLAB_WOOD;
		int lower = ComponentTFTowerWing.SLAB_LOWER;
		int doubled = ComponentTFTowerWing.SLAB_DOUBLE;
		int slopeChange = slopeChangeForSize(this.size);

		for (int y = 0; y <= this.height; y++) {
			int min;
			int max;
			if (y < slopeChange) {
				min = y;
				max = this.size - y - 1;
			} else {
				min = (y + slopeChange) / 2;
				max = this.size - (y + slopeChange) / 2 - 1;
			}

			for (int x = 0; x <= this.size - 2; x++) {
				for (int z = min; z <= max; z++) {
					if (z == min || z == max) {

						placeBlock(world, slabId, doubled, x, y, z, clip);
					} else if (x < this.size - 2) {
						placeBlock(world, ComponentTFTowerWing.SLAB_STONE_BRICK, doubled,
							x, y, z, clip);
					}
				}
			}
		}

		int top = this.size + 1 - slopeChange;
		int zMid = this.size / 2;
		placeBlock(world, slabId, lower, 0, top, zMid, clip);
		placeBlock(world, slabId, lower, this.size - 3, top, zMid, clip);
		placeBlock(world, slabId, doubled, this.size - 2, top, zMid, clip);
		placeBlock(world, slabId, doubled, this.size - 1, top, zMid, clip);
		placeBlock(world, slabId, lower, this.size - 1, top + 1, zMid, clip);
		return true;
	}

	public int slopeChangeForSize(int pSize) {
		if (this.size > 10) {
			return 3;
		}
		return this.size > 6 ? 2 : 1;
	}
}
