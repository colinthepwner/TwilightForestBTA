package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeUpperEntrance extends StructureComponentTF {

	private static final int SIZE = 16;

	public ComponentTFMazeUpperEntrance(int componentType, Random rand, int x, int y, int z) {
		super(componentType);

		this.coordBaseMode = rand.nextInt(4);
		this.boundingBox = new BoundingBox(x, y, z, x + SIZE - 1, y + 4, z + SIZE - 1);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int plain = ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_PLAIN);
		int mosaic = ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_MOSAIC);
		int deco = ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_DECO);
		int brick = ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_BRICK);
		int fence = Blocks.FENCE_PLANKS_OAK.id();
		int bars = Blocks.FENCE_STEEL.id();

		Random decay = new Random(world.getRandomSeed()
			+ (long) this.boundingBox.minX * this.boundingBox.minZ);

		randomlyFillWithBlocks(world, clip, decay, 0.7f, 0, 5, 0, 15, 5, 15, plain, 0, true);

		fillWithBlocks(world, clip, 0, 0, 0, 15, 0, 15, mosaic, 0, 0, 0, false);
		fillWithBlocks(world, clip, 0, 1, 0, 15, 1, 15, deco, 0, 0, 0, true);
		fillWithBlocks(world, clip, 0, 2, 0, 15, 3, 15, brick, 0, 0, 0, true);
		fillWithBlocks(world, clip, 0, 4, 0, 15, 4, 15, deco, 0, 0, 0, true);

		randomlyFillWithBlocks(world, clip, decay, 0.2f, 0, 0, 0, 15, 5, 15,
			Blocks.GRAVEL.id(), 0, true);

		fillWithBlocks(world, clip, 6, 1, 0, 9, 4, 0, fence, 0, false);
		fillWithBlocks(world, clip, 7, 1, 0, 8, 3, 0, 0, 0, false);
		fillWithBlocks(world, clip, 6, 1, 15, 9, 4, 15, fence, 0, false);
		fillWithBlocks(world, clip, 7, 1, 15, 8, 3, 15, 0, 0, false);
		fillWithBlocks(world, clip, 0, 1, 6, 0, 4, 9, fence, 0, false);
		fillWithBlocks(world, clip, 0, 1, 7, 0, 3, 8, 0, 0, false);
		fillWithBlocks(world, clip, 15, 1, 6, 15, 4, 9, fence, 0, false);
		fillWithBlocks(world, clip, 15, 1, 7, 15, 3, 8, 0, 0, false);

		fillWithBlocks(world, clip, 1, 1, 1, 14, 4, 14, 0, 0, false);

		fillWithBlocks(world, clip, 5, 1, 5, 10, 1, 10, deco, 0, 0, 0, false);
		fillWithBlocks(world, clip, 5, 4, 5, 10, 4, 10, deco, 0, 0, 0, false);

		randomlyFillWithBlocks(world, clip, decay, 0.7f, 5, 2, 5, 10, 3, 10, bars, 0, false);

		fillWithBlocks(world, clip, 6, 0, 6, 9, 4, 9, 0, 0, false);
		return true;
	}

	protected void randomlyFillWithBlocks(World world, BoundingBox clip, Random rand, float chance,
	                                      int minX, int minY, int minZ,
	                                      int maxX, int maxY, int maxZ,
	                                      int blockId, int meta, boolean alwaysReplace) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (rand.nextFloat() > chance) {
						continue;
					}
					if (alwaysReplace && getBlockIdAt(world, x, y, z, clip) == 0) {
						continue;
					}
					boolean shell = y == minY || y == maxY
						|| x == minX || x == maxX
						|| z == minZ || z == maxZ;
					if (shell) {
						placeBlock(world, blockId, meta, x, y, z, clip);
					} else {
						placeBlock(world, 0, 0, x, y, z, clip);
					}
				}
			}
		}
	}
}
