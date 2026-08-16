package com.twilightforest.world.structure;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndShrooms extends ComponentTFMazeDeadEndRoots {

	private static final int MYCELIUM_STANDIN = Blocks.MUD.id();

	private static final int MUSHROOM_BROWN = Blocks.MUSHROOM_BROWN.id();
	private static final int MUSHROOM_RED = Blocks.MUSHROOM_RED.id();

	public ComponentTFMazeDeadEndShrooms(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int giantBrown = TFBlocks.MUSHROOM_GIANT_BROWN.id();
		int giantRed = TFBlocks.MUSHROOM_GIANT_RED.id();

		for (int x = 1; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				if (rand.nextInt(z + 2) > 0) {
					placeBlock(world, MYCELIUM_STANDIN, 0, x, 0, z, clip);
				}
				if (rand.nextInt(z + 2) <= 0) {
					continue;
				}
				placeBlock(world, rand.nextBoolean() ? MUSHROOM_RED : MUSHROOM_BROWN, 0, x, 1, z, clip);
			}
		}

		int mushType = rand.nextBoolean() ? giantRed : giantBrown;
		int mushY = rand.nextInt(4) + 1;
		int mushZ = rand.nextInt(3) + 1;

		placeBlock(world, mushType, BlockLogicTFGiantMushroom.STEM, 1, mushY - 1, mushZ, clip);
		fillWithBlocks(world, clip, 1, 1, mushZ, 1, mushY, mushZ,
			mushType, BlockLogicTFGiantMushroom.STEM, AIR, 0, false);
		fillWithBlocks(world, clip, 1, mushY, mushZ - 1, 2, mushY, mushZ + 1,
			mushType, BlockLogicTFGiantMushroom.ALL_SKIN, AIR, 0, false);

		mushType = mushType == giantBrown ? giantRed : giantBrown;
		mushY = rand.nextInt(4) + 1;
		mushZ = rand.nextInt(3) + 1;

		fillWithBlocks(world, clip, 4, 1, mushZ, 4, mushY, mushZ,
			mushType, BlockLogicTFGiantMushroom.STEM, AIR, 0, false);
		fillWithBlocks(world, clip, 3, mushY, mushZ - 1, 4, mushY, mushZ + 1,
			mushType, BlockLogicTFGiantMushroom.ALL_SKIN, AIR, 0, false);

		mushType = rand.nextBoolean() ? giantRed : giantBrown;
		mushY = rand.nextInt(4) + 1;
		int mushX = rand.nextInt(3) + 2;

		fillWithBlocks(world, clip, mushX, 1, 4, mushX, mushY, 4,
			mushType, BlockLogicTFGiantMushroom.STEM, AIR, 0, false);
		fillWithBlocks(world, clip, mushX - 1, mushY, 3, mushX + 1, mushY, 4,
			mushType, BlockLogicTFGiantMushroom.ALL_SKIN, AIR, 0, false);

		return true;
	}
}
