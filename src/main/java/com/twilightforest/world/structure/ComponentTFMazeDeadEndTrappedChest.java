package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeDeadEndTrappedChest extends ComponentTFMazeDeadEndChest {

	private static final int TNT = Blocks.TNT.id();

	private static final int PRESSURE_PLATE = Blocks.PRESSURE_PLATE_PLANKS_OAK.id();

	public ComponentTFMazeDeadEndTrappedChest(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		placeBlock(world, PRESSURE_PLATE, 0, 2, 1, 2, clip);
		placeBlock(world, PRESSURE_PLATE, 0, 3, 1, 2, clip);

		placeBlock(world, TNT, 0, 0, 0, 2, clip);
		placeBlock(world, AIR, 0, 0, -1, 2, clip);
		placeBlock(world, AIR, 0, 1, -1, 2, clip);

		placeBlock(world, TNT, 0, 2, 0, 4, clip);
		placeBlock(world, TNT, 0, 3, 0, 4, clip);
		placeBlock(world, TNT, 0, 2, 0, 3, clip);
		placeBlock(world, TNT, 0, 3, 0, 3, clip);

		return true;
	}
}
