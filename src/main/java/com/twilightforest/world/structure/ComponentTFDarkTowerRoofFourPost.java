package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFDarkTowerRoofFourPost extends ComponentTFDarkTowerRoof {

	public ComponentTFDarkTowerRoofFourPost(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		super.addComponentParts(world, rand, clip);

		makeSmallAntenna(world, clip, 4, this.size - 2, this.size - 2);
		makeSmallAntenna(world, clip, 5, 1, this.size - 2);
		makeSmallAntenna(world, clip, 6, this.size - 2, 1);
		makeSmallAntenna(world, clip, 7, 1, 1);
		return true;
	}

	protected void makeSmallAntenna(World world, BoundingBox clip, int height, int x, int z) {
		for (int y = 1; y < height; y++) {
			placeBlock(world, this.deco.blockID, this.deco.blockMeta, x, y, z, clip);
		}

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, height + 0, z, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x + 1, height + 1, z, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x - 1, height + 1, z, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z - 1, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, height + 2, z, clip);
	}
}
