package com.twilightforest.world.structure;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.BlockLogicStairs;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomSpawnerChests extends ComponentTFMazeRoom {

	private static final int PLANKS = Blocks.PLANKS_OAK.id();

	private static final int STAIRS = Blocks.STAIRS_PLANKS_OAK.id();

	private static final int BARS = Blocks.FENCE_STEEL.id();

	private static final int PRESSURE_PLATE = Blocks.PRESSURE_PLATE_PLANKS_OAK.id();

	private static final int TNT = Blocks.TNT.id();

	private static final String MINOTAUR = TwilightForest.MOD_ID + ":minotaur";

	public ComponentTFMazeRoomSpawnerChests(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		placePillarEnclosure(world, clip, 3, 3);
		placePillarEnclosure(world, clip, 10, 3);
		placePillarEnclosure(world, clip, 3, 10);
		placePillarEnclosure(world, clip, 10, 10);

		placeSpawner(world, rand, 4, 2, 4, MINOTAUR, clip);
		placeTreasure(world, rand, 4, 2, 11, TFTreasure.LABYRINTH_ROOM, clip);
		placeTreasure(world, rand, 11, 2, 4, TFTreasure.LABYRINTH_ROOM, clip);

		placeBlock(world, PRESSURE_PLATE, 0, 11, 1, 11, clip);
		placeBlock(world, TNT, 0, 10, 0, 11, clip);
		placeBlock(world, TNT, 0, 11, 0, 10, clip);
		placeBlock(world, TNT, 0, 11, 0, 12, clip);
		placeBlock(world, TNT, 0, 12, 0, 11, clip);

		return true;
	}

	private void placePillarEnclosure(World world, BoundingBox clip, int dx, int dz) {

		int pillar = TFBlocks.MAZESTONE_COBBLE.id();

		for (int y = 1; y < 5; y++) {
			placeBlock(world, pillar, 0, dx + 0, y, dz + 0, clip);
			placeBlock(world, pillar, 0, dx + 2, y, dz + 0, clip);
			placeBlock(world, pillar, 0, dx + 0, y, dz + 2, clip);
			placeBlock(world, pillar, 0, dx + 2, y, dz + 2, clip);
		}

		placeBlock(world, PLANKS, 0, dx + 1, 1, dz + 1, clip);
		placeBlock(world, PLANKS, 0, dx + 1, 4, dz + 1, clip);

		placeBlock(world, STAIRS, getStairMeta(1), dx + 1, 1, dz + 0, clip);
		placeBlock(world, STAIRS, getStairMeta(0), dx + 0, 1, dz + 1, clip);
		placeBlock(world, STAIRS, getStairMeta(2), dx + 2, 1, dz + 1, clip);
		placeBlock(world, STAIRS, getStairMeta(3), dx + 1, 1, dz + 2, clip);

		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;
		placeBlock(world, STAIRS, getStairMeta(1) | flip, dx + 1, 4, dz + 0, clip);
		placeBlock(world, STAIRS, getStairMeta(0) | flip, dx + 0, 4, dz + 1, clip);
		placeBlock(world, STAIRS, getStairMeta(2) | flip, dx + 2, 4, dz + 1, clip);
		placeBlock(world, STAIRS, getStairMeta(3) | flip, dx + 1, 4, dz + 2, clip);

		for (int y = 2; y < 4; y++) {
			placeBlock(world, BARS, 0, dx + 1, y, dz + 0, clip);
			placeBlock(world, BARS, 0, dx + 0, y, dz + 1, clip);
			placeBlock(world, BARS, 0, dx + 2, y, dz + 1, clip);
			placeBlock(world, BARS, 0, dx + 1, y, dz + 2, clip);
		}
	}
}
