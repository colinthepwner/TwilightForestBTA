package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeCorridor extends StructureComponentTF {

	protected static final int FENCE = Blocks.FENCE_PLANKS_OAK.id();

	protected static final int BARS = Blocks.FENCE_STEEL.id();

	protected static final int AIR = Blocks.AIR.id();

	public ComponentTFMazeCorridor(int componentType, int x, int y, int z, int rotation) {
		super(componentType);
		this.coordBaseMode = rotation;
		this.boundingBox = new BoundingBox(x, y, z, x + 5, y + 5, z + 5);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		fillWithBlocks(world, clip, 1, 1, 2, 4, 4, 3, FENCE, 0, AIR, 0, false);
		fillWithBlocks(world, clip, 2, 1, 2, 3, 3, 3, AIR, 0, false);
		return true;
	}
}
