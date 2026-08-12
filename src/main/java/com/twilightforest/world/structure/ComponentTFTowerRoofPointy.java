package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofPointy extends ComponentTFTowerRoof {

	public ComponentTFTowerRoofPointy(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size;
		this.height = this.size;
		makeCapBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		for (int y = 0; y <= this.height; y++) {
			int slopeChange = slopeChangeForSize(this.size);
			int min;
			int max;
			if (y < slopeChange) {
				min = y;
				max = this.size - y - 1;
			} else {
				min = (y + slopeChange) / 2;
				max = this.size - (y + slopeChange) / 2 - 1;
			}
			int mid = min + (max - min) / 2;

			for (int x = min; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
						ComponentTFTowerWing.SLAB_DOUBLE, x, y, z, clip);

					boolean corner = (x == min || x == max) && (z == min || z == max);
					boolean edgeMid =
						(x == min || x == max) && z == mid && x % 2 == 0
							|| (z == min || z == max) && x == mid && z % 2 == 0;

					if (corner || (edgeMid && mid != min + 1)) {
						placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
							ComponentTFTowerWing.SLAB_LOWER, x, y + 1, z, clip);
					}
				}
			}
		}
		return true;
	}

	public int slopeChangeForSize(int pSize) {
		if (this.size > 10) {
			return 3;
		}
		return this.size > 6 ? 2 : 1;
	}
}
