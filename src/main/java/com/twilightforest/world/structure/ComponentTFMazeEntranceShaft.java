package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeEntranceShaft extends StructureComponentTF {

	private static final int SIZE = 6;

	private int averageGroundLevel = -1;

	public ComponentTFMazeEntranceShaft(int componentType, Random rand, int x, int y, int z) {
		super(componentType);

		this.coordBaseMode = rand.nextInt(4);
		this.boundingBox = new BoundingBox(x, y, z, x + SIZE - 1, y + 14, z + SIZE - 1);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = getAverageGroundLevel(world, clip);
			if (this.averageGroundLevel < 0) {

				return true;
			}

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 12 - 1, 0);
		}

		fillWithBlocks(world, clip, 0, -10, 0, SIZE - 1, 30, SIZE - 1,
			ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_BRICK), 0,
			0, 0, true);

		fillWithBlocks(world, clip, 1, -10, 1, SIZE - 2, 30, SIZE - 2, 0, 0, false);
		return true;
	}

	protected int getAverageGroundLevel(World world, BoundingBox clip) {
		int total = 0;
		int count = 0;
		for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
			for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
				if (!clip.contains(x, 64, z)) {
					continue;
				}
				total += Math.max(world.getHeightValue(x, z), StructureStartTF.BASE_Y);
				count++;
			}
		}
		if (count == 0) {
			return -1;
		}
		return total / count;
	}
}
