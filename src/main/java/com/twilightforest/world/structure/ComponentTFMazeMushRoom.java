package com.twilightforest.world.structure;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeMushRoom extends ComponentTFMazeRoom {

	private static final int MYCELIUM_STANDIN = Blocks.MUD.id();

	private static final int MUSHROOM_BROWN = Blocks.MUSHROOM_BROWN.id();
	private static final int MUSHROOM_RED = Blocks.MUSHROOM_RED.id();

	public ComponentTFMazeMushRoom(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
		this.coordBaseMode = 0;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		int giantBrown = TFBlocks.MUSHROOM_GIANT_BROWN.id();
		int giantRed = TFBlocks.MUSHROOM_GIANT_RED.id();

		for (int x = 1; x < 14; x++) {
			for (int z = 1; z < 14; z++) {
				int dist = (int) Math.round(
					7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));

				if (rand.nextInt(dist + 1) > 0) {
					placeBlock(world, MYCELIUM_STANDIN, 0, x, 0, z, clip);
				}
				if (rand.nextInt(dist) <= 0) {
					continue;
				}
				placeBlock(world, rand.nextBoolean() ? MUSHROOM_RED : MUSHROOM_BROWN, 0, x, 1, z, clip);
			}
		}

		makeMediumMushroom(world, clip, 5, 2, 9, giantRed);
		makeMediumMushroom(world, clip, 5, 3, 9, giantRed);
		makeMediumMushroom(world, clip, 9, 2, 5, giantRed);
		makeMediumMushroom(world, clip, 6, 3, 4, giantBrown);
		makeMediumMushroom(world, clip, 10, 1, 9, giantBrown);

		placeBlock(world, giantRed, BlockLogicTFGiantMushroom.STEM, 1, 2, 1, clip);
		placeBlock(world, giantRed, 5, 1, 3, 1, clip);
		placeBlock(world, giantRed, 9, 2, 3, 1, clip);
		placeBlock(world, giantRed, 9, 1, 3, 2, clip);

		placeBlock(world, giantBrown, BlockLogicTFGiantMushroom.STEM, 14, 3, 1, clip);
		placeBlock(world, giantBrown, 5, 14, 4, 1, clip);
		placeBlock(world, giantBrown, 7, 13, 4, 1, clip);
		placeBlock(world, giantBrown, 7, 14, 4, 2, clip);

		placeBlock(world, giantBrown, BlockLogicTFGiantMushroom.STEM, 1, 1, 14, clip);
		placeBlock(world, giantBrown, 5, 1, 2, 14, clip);
		placeBlock(world, giantBrown, 3, 2, 2, 14, clip);
		placeBlock(world, giantBrown, 3, 1, 2, 13, clip);

		placeBlock(world, giantBrown, 5, 14, 1, 14, clip);
		placeBlock(world, giantBrown, 1, 13, 1, 14, clip);
		placeBlock(world, giantBrown, 1, 14, 1, 13, clip);

		return true;
	}

	private void makeMediumMushroom(World world, BoundingBox clip, int mx, int my, int mz, int blockId) {
		placeBlock(world, blockId, 5, mx + 0, my, mz + 0, clip);
		placeBlock(world, blockId, 6, mx + 1, my, mz + 0, clip);
		placeBlock(world, blockId, 9, mx + 1, my, mz + 1, clip);
		placeBlock(world, blockId, 8, mx + 0, my, mz + 1, clip);
		placeBlock(world, blockId, 7, mx - 1, my, mz + 1, clip);
		placeBlock(world, blockId, 4, mx - 1, my, mz + 0, clip);
		placeBlock(world, blockId, 1, mx - 1, my, mz - 1, clip);
		placeBlock(world, blockId, 2, mx + 0, my, mz - 1, clip);
		placeBlock(world, blockId, 3, mx + 1, my, mz - 1, clip);

		for (int y = 1; y < my; y++) {
			placeBlock(world, blockId, BlockLogicTFGiantMushroom.STEM, mx + 0, y, mz + 0, clip);
		}
	}
}
