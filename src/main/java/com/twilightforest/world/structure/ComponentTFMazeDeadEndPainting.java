package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndPainting extends ComponentTFMazeDeadEnd {

	private static final int TORCH = Blocks.TORCH_COAL.id();

	public ComponentTFMazeDeadEndPainting(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		placeBlock(world, TORCH, 0, 1, 3, 3, clip);
		placeBlock(world, TORCH, 0, 4, 3, 3, clip);

		return true;
	}
}
