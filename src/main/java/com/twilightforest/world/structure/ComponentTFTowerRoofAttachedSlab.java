package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerRoofAttachedSlab extends ComponentTFTowerRoofSlab {

	public ComponentTFTowerRoofAttachedSlab(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return makeConnectedCap(world, clip);
	}
}
