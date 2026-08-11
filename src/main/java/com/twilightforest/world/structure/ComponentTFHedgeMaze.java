package com.twilightforest.world.structure;

import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.WorldFeatureTFHedgeMaze;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFHedgeMaze extends StructureComponentTF {

	private static final int MAZE_CELLS = 16;
	private static final int RADIUS = MAZE_CELLS / 2 * 3 + 1;

	private final int centreX;
	private final int centreY;
	private final int centreZ;

	public ComponentTFHedgeMaze(int componentType, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = 0;
		this.centreX = x;
		this.centreY = y;
		this.centreZ = z;
		this.boundingBox = componentBox(x, y, z,
			-RADIUS, -3, -RADIUS, RADIUS * 2, 10, RADIUS * 2, 0);
	}

	public static int floorY() {
		return TFWorldConstants.SEA_LEVEL + 1;
	}

	@Override
	public int featureType() {
		return 0;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		Random mazeRand = new Random(world.getRandomSeed()
			+ (long) this.boundingBox.minX * this.boundingBox.minZ);

		if (!clip.contains(this.centreX, this.centreY, this.centreZ)) {
			return true;
		}

		new WorldFeatureTFHedgeMaze(1).place(world, mazeRand, this.centreX, this.centreY, this.centreZ);
		return true;
	}
}
