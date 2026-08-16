package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFDarkTowerRoofCactus extends ComponentTFDarkTowerRoof {

	public ComponentTFDarkTowerRoofCactus(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		super.addComponentParts(world, rand, clip);

		int cx = this.size / 2;
		int cz = this.size / 2;

		for (int y = 1; y < 10; y++) {
			placeBlock(world, this.deco.blockID, this.deco.blockMeta, cx, y, cz, clip);
		}
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 10, cz, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, 1, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, 1, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 1, cz - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 1, cz + 1, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, 7, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, 7, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, 8, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, 9, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 3, 9, cz, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 6, cz + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 6, cz + 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 7, cz + 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 8, cz + 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 8, cz + 3, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, 5, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, 5, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, 6, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, 7, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 3, 7, cz, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 4, cz - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 4, cz - 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 5, cz - 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 6, cz - 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 6, cz - 3, clip);
		return true;
	}
}
