package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofFence extends ComponentTFTowerRoof {

	public ComponentTFTowerRoofFence(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size;
		this.height = 0;
		makeCapBB(wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int y = this.height + 1;

		for (int x = 0; x <= this.size - 1; x++) {
			for (int z = 0; z <= this.size - 1; z++) {
				if (x == 0 || x == this.size - 1 || z == 0 || z == this.size - 1) {
					placeBlock(world, ComponentTFTowerWing.FENCE, 0, x, y, z, clip);
				}
			}
		}
		return true;
	}
}
