package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofSlabForwards extends ComponentTFTowerRoofSlab {

	public ComponentTFTowerRoofSlabForwards(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size + 2;
		this.height = this.size / 2;
		makeAttachedOverhangBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		for (int y = 0; y <= this.height; y++) {
			int min = 2 * y;
			int max = this.size - 2 * y - 1;

			for (int x = 0; x <= max - 1; x++) {
				for (int z = min; z <= max; z++) {
					boolean interior = x != max - 1 && z != min && z != max;
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
