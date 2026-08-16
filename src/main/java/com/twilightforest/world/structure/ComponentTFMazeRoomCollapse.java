package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomCollapse extends ComponentTFMazeRoom {

	private static final int GRAVEL = Blocks.GRAVEL.id();

	private static final int DIRT = Blocks.DIRT.id();

	public ComponentTFMazeRoomCollapse(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		super.addComponentParts(world, rand, clip);

		int rootStrands = TFBlocks.ROOT_STRANDS.id();

		for (int x = 1; x < 14; x++) {
			for (int z = 1; z < 14; z++) {
				int dist = (int) Math.round(
					7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));

				int gravel = rand.nextInt(dist);
				int root = rand.nextInt(dist);

				if (gravel > 0) {

					gravel++;
					fillWithBlocks(world, clip, x, 1, z, x, gravel, z, GRAVEL, 0, false);
					fillWithBlocks(world, clip, x, gravel, z, x, gravel + 5, z, AIR, 0, false);
					continue;
				}

				if (root > 0) {

					fillWithBlocks(world, clip, x, 5, z, x, 5 + root, z, DIRT, 0, AIR, 0, true);
					fillWithBlocks(world, clip, x, 5 - rand.nextInt(5), z, x, 5, z,
						rootStrands, 0, AIR, 0, false);
					continue;
				}

				if (rand.nextInt(dist + 1) <= 0) {
					continue;
				}
				fillWithBlocks(world, clip, x, 5, z, x, 5, z, AIR, 0, false);
			}
		}
		return true;
	}
}
