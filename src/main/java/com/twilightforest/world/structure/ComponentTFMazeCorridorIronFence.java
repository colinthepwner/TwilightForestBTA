package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeCorridorIronFence extends ComponentTFMazeCorridor {

	public ComponentTFMazeCorridorIronFence(int componentType, int x, int y, int z, int rotation) {
		super(componentType, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		int deco = TFBlocks.MAZESTONE_MOSSY.id();

		int pillar = TFBlocks.MAZESTONE_COBBLE.id();

		fillWithBlocks(world, clip, 1, 4, 2, 4, 4, 3, deco, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 1, 2, 4, 3, 3, pillar, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 2, 1, 2, 3, 3, 3, BARS, 0, false);
		return true;
	}
}
