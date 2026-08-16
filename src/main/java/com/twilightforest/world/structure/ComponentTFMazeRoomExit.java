package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomExit extends ComponentTFMazeRoom {

	private static final int BARS = Blocks.FENCE_STEEL.id();

	public ComponentTFMazeRoomExit(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		int brick = TFBlocks.MAZESTONE.id();
		int deco = TFBlocks.MAZESTONE_MOSSY.id();

		fillWithBlocks(world, clip, 5, -5, 5, 10, 0, 10, brick, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 1, 5, 10, 1, 10, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 2, 5, 10, 3, 10, BARS, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 4, 5, 10, 4, 10, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, -5, 6, 9, 4, 9, AIR, 0, false);

		return true;
	}
}
