package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoom extends StructureComponentTF {

	protected static final int FENCE = Blocks.FENCE_PLANKS_OAK.id();

	protected static final int AIR = Blocks.AIR.id();

	public ComponentTFMazeRoom(int componentType, Random rand, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = rand.nextInt(4);
		this.boundingBox = new BoundingBox(x, y, z, x + 15, y + 4, z + 15);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		int border = TFBlocks.MAZESTONE_COBBLE.id();
		int mosaic = TFBlocks.MAZESTONE.id();

		fillWithBlocks(world, clip, 1, 0, 1, 14, 0, 14, border, 0, AIR, 0, true);
		fillWithBlocks(world, clip, 2, 0, 2, 13, 0, 13, mosaic, 0, AIR, 0, true);

		if (getBlockIdAt(world, 7, 1, 0, clip) == 0) {
			fillWithBlocks(world, clip, 6, 1, 0, 9, 4, 0, FENCE, 0, false);
			fillWithBlocks(world, clip, 7, 1, 0, 8, 3, 0, AIR, 0, false);
		}
		if (getBlockIdAt(world, 7, 1, 15, clip) == 0) {
			fillWithBlocks(world, clip, 6, 1, 15, 9, 4, 15, FENCE, 0, false);
			fillWithBlocks(world, clip, 7, 1, 15, 8, 3, 15, AIR, 0, false);
		}
		if (getBlockIdAt(world, 0, 1, 7, clip) == 0) {
			fillWithBlocks(world, clip, 0, 1, 6, 0, 4, 9, FENCE, 0, false);
			fillWithBlocks(world, clip, 0, 1, 7, 0, 3, 8, AIR, 0, false);
		}
		if (getBlockIdAt(world, 15, 1, 7, clip) == 0) {
			fillWithBlocks(world, clip, 15, 1, 6, 15, 4, 9, FENCE, 0, false);
			fillWithBlocks(world, clip, 15, 1, 7, 15, 3, 8, AIR, 0, false);
		}

		return true;
	}
}
