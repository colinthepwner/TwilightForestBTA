package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofStairsOverhang extends ComponentTFTowerRoofStairs {

	public ComponentTFTowerRoofStairsOverhang(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = 0;
		this.size = wing.size + 2;
		this.height = this.size / 2;
		this.boundingBox = new BoundingBox(
			wing.boundingBox.minX - 1, wing.boundingBox.maxY, wing.boundingBox.minZ - 1,
			wing.boundingBox.maxX + 1, wing.boundingBox.maxY + this.height - 1,
			wing.boundingBox.maxZ + 1);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return buildHippedRoof(world, clip);
	}
}
