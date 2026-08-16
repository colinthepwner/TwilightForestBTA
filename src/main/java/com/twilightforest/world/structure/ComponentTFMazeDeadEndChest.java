package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndChest extends ComponentTFMazeDeadEnd {

	protected static final int PLANKS = Blocks.PLANKS_OAK.id();

	protected static final int STAIRS = Blocks.STAIRS_PLANKS_OAK.id();

	protected static final int CHEST = Blocks.CHEST_PLANKS_OAK.id();

	public ComponentTFMazeDeadEndChest(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
		this.spawnListIndex = Integer.MAX_VALUE;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		int pillar = TFBlocks.MAZESTONE_COBBLE.id();
		int deco = TFBlocks.MAZESTONE_MOSSY.id();

		placeBlock(world, PLANKS, 0, 2, 1, 4, clip);
		placeBlock(world, PLANKS, 0, 3, 1, 4, clip);
		placeBlock(world, STAIRS, getStairMeta(1), 2, 1, 3, clip);
		placeBlock(world, STAIRS, getStairMeta(1), 3, 1, 3, clip);

		placeBlock(world, CHEST, 0, 2, 2, 4, clip);
		placeTreasure(world, rand, 3, 2, 4, TFTreasure.LABYRINTH_DEADEND, clip);

		fillWithBlocks(world, clip, 1, 1, 0, 4, 3, 1, pillar, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 4, 0, 4, 4, 1, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 2, 1, 0, 3, 3, 1, BARS, 0, false);

		return true;
	}
}
