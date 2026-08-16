package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFDarkTowerRoofRings extends ComponentTFDarkTowerRoof {

	public ComponentTFDarkTowerRoofRings(int componentType, ComponentTFTowerWing wing) {
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

		makeARing(world, 6, clip);
		makeARing(world, 8, clip);
		return true;
	}

	protected void makeARing(World world, int y, BoundingBox clip) {
		int cx = this.size / 2;
		int cz = this.size / 2;

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, y, cz + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, y, cz + 0, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 2, y, cz - 1, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, y, cz + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, y, cz + 0, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 2, y, cz - 1, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, y, cz - 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 0, y, cz - 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, y, cz - 2, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, y, cz + 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 0, y, cz + 2, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, y, cz + 2, clip);
	}
}
