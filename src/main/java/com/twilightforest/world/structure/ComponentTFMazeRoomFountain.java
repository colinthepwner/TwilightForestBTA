package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomFountain extends ComponentTFMazeRoom {

	private static final int WATER = Blocks.FLUID_WATER_FLOWING.id();

	public ComponentTFMazeRoomFountain(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		int deco = TFBlocks.MAZESTONE_MOSSY.id();

		fillWithBlocks(world, clip, 5, 1, 5, 10, 1, 10, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, 1, 6, 9, 1, 9, WATER, 0, false);

		return true;
	}
}
