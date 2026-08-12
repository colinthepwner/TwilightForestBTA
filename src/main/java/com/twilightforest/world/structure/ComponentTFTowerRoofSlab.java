package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofSlab extends ComponentTFTowerRoof {

	public ComponentTFTowerRoofSlab(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size;
		this.height = this.size / 2;
		makeCapBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return makePyramidCap(world, clip);
	}

	protected boolean makePyramidCap(World world, BoundingBox clip) {
		for (int y = 0; y <= this.height; y++) {
			int min = 2 * y;
			int max = this.size - 2 * y - 1;

			for (int x = min; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					boolean interior = x != min && x != max && z != min && z != max;
					placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
						interior ? ComponentTFTowerWing.SLAB_DOUBLE
							: ComponentTFTowerWing.SLAB_LOWER,
						x, y, z, clip);
				}
			}
		}
		return true;
	}

	protected boolean makeConnectedCap(World world, BoundingBox clip) {
		for (int y = 0; y < this.height; y++) {
			int min = 2 * y;
			int max = this.size - 2 * y - 1;

			for (int x = 0; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					boolean interior = x != max && z != min && z != max;
					placeBlock(world, ComponentTFTowerWing.SLAB_WOOD,
						interior ? ComponentTFTowerWing.SLAB_DOUBLE
							: ComponentTFTowerWing.SLAB_LOWER,
						x, y, z, clip);
				}
			}
		}
		return true;
	}
}
