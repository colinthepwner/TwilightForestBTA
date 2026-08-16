package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndFountain extends ComponentTFMazeDeadEnd {

	protected static final int WATER = Blocks.FLUID_WATER_FLOWING.id();

	public ComponentTFMazeDeadEndFountain(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		int brick = TFBlocks.MAZESTONE.id();

		fillWithBlocks(world, clip, 1, 1, 4, 4, 4, 4, brick, 0, AIR, 0, false);

		placeBlock(world, WATER, 0, 2, 3, 4, clip);
		placeBlock(world, WATER, 0, 3, 3, 4, clip);

		placeBlock(world, AIR, 0, 2, 0, 3, clip);
		placeBlock(world, AIR, 0, 3, 0, 3, clip);

		return true;
	}
}
