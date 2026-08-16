package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomVault extends ComponentTFMazeRoom {

	private static final int PRESSURE_PLATE = Blocks.PRESSURE_PLATE_PLANKS_OAK.id();

	private static final int SAND = Blocks.SAND.id();

	private static final int TNT = Blocks.TNT.id();

	private static final int CHEST = Blocks.CHEST_PLANKS_OAK.id();

	public ComponentTFMazeRoomVault(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
		this.spawnListIndex = Integer.MAX_VALUE;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		int deco = TFBlocks.MAZESTONE_MOSSY.id();
		int brick = TFBlocks.MAZESTONE.id();

		fillWithBlocks(world, clip, 0, 1, 0, 15, 4, 15, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 0, 2, 0, 15, 3, 15, brick, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, 2, 6, 9, 3, 9, AIR, 0, false);

		fillWithBlocks(world, clip, 6, 2, 5, 9, 2, 5, PRESSURE_PLATE, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, 2, 10, 9, 2, 10, PRESSURE_PLATE, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 2, 6, 5, 2, 9, PRESSURE_PLATE, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 10, 2, 6, 10, 2, 9, PRESSURE_PLATE, 0, AIR, 0, false);

		fillWithBlocks(world, clip, 6, 4, 5, 9, 4, 5, SAND, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, 4, 10, 9, 4, 10, SAND, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 4, 6, 5, 4, 9, SAND, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 10, 4, 6, 10, 4, 9, SAND, 0, AIR, 0, false);

		fillWithBlocks(world, clip, 6, 0, 5, 9, 0, 5, TNT, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 6, 0, 10, 9, 0, 10, TNT, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 5, 0, 6, 5, 0, 9, TNT, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 10, 0, 6, 10, 0, 9, TNT, 0, AIR, 0, false);

		placeBlock(world, CHEST, 0, 7, 2, 6, clip);
		placeTreasure(world, rand, 8, 2, 6, TFTreasure.LABYRINTH_VAULT, clip);
		placeBlock(world, CHEST, 0, 8, 2, 9, clip);
		placeTreasure(world, rand, 7, 2, 9, TFTreasure.LABYRINTH_VAULT, clip);
		placeBlock(world, CHEST, 0, 6, 2, 7, clip);
		placeTreasure(world, rand, 6, 2, 8, TFTreasure.LABYRINTH_VAULT, clip);
		placeBlock(world, CHEST, 0, 9, 2, 8, clip);
		placeTreasure(world, rand, 9, 2, 7, TFTreasure.LABYRINTH_VAULT, clip);

		return true;
	}
}
