package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFDarkTowerRoofAntenna extends ComponentTFDarkTowerRoof {

	public ComponentTFDarkTowerRoofAntenna(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		super.addComponentParts(world, rand, clip);

		int cx = this.size / 2;
		int cz = this.size / 2;

		for (int y = 1; y < 10; y++) {
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, y, cz, clip);
		}

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, 1, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, 1, cz, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 1, cz - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, 1, cz + 1, clip);

		for (int y = 7; y < 10; y++) {
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, y, cz, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, y, cz, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, y, cz - 1, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx, y, cz + 1, clip);
		}

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, 8, cz - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx - 1, 8, cz + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, 8, cz - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, cx + 1, 8, cz + 1, clip);
		return true;
	}
}
