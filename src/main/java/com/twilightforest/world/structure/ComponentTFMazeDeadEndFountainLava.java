package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndFountainLava extends ComponentTFMazeDeadEndFountain {

	private static final int LAVA = Blocks.FLUID_LAVA_FLOWING.id();

	public ComponentTFMazeDeadEndFountainLava(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		placeBlock(world, AIR, 0, 2, 3, 4, clip);
		placeBlock(world, AIR, 0, 3, 3, 4, clip);
		placeBlock(world, LAVA, 0, 2, 3, 4, clip);
		placeBlock(world, LAVA, 0, 3, 3, 4, clip);

		return true;
	}
}
