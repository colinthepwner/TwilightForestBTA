package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndTorches extends ComponentTFMazeDeadEnd {

	private static final int TORCH = Blocks.TORCH_COAL.id();

	public ComponentTFMazeDeadEndTorches(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		fillWithBlocks(world, clip, 2, 1, 4, 3, 4, 4, TORCH, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 1, 1, 1, 4, 4, TORCH, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 4, 1, 1, 4, 4, 4, TORCH, 0, AIR, 0, false);

		return true;
	}
}
