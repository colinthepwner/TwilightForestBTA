package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeCorridorRoots extends ComponentTFMazeCorridor {

	private static final int DIRT = Blocks.DIRT.id();

	private static final int GRAVEL = Blocks.GRAVEL.id();

	public ComponentTFMazeCorridorRoots(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		int rootStrands = TFBlocks.ROOT_STRANDS.id();

		for (int x = 1; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				int freq = x;
				if (rand.nextInt(freq + 2) <= 0) {
					continue;
				}

				int length = rand.nextInt(6);
				placeBlock(world, DIRT, 0, x, 6, z, clip);
				for (int y = 6 - length; y < 6; y++) {
					placeBlock(world, rootStrands, 0, x, y, z, clip);
				}

				if (rand.nextInt(freq + 1) <= 1) {
					continue;
				}
				placeBlock(world, GRAVEL, 0, x, 1, z, clip);

				if (rand.nextInt(freq + 1) <= 1) {
					continue;
				}
				placeBlock(world, GRAVEL, 0, x, 2, z, clip);
			}
		}
		return true;
	}
}
