package com.twilightforest.world.structure;

import com.twilightforest.world.feature.WorldFeatureTFHillMaze;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFHillMaze extends StructureComponentTF {

	public static final int DEPTH_BELOW_CHAMBER = 20;

	private final int hsize;
	private final int centreX;
	private final int centreY;
	private final int centreZ;

	public ComponentTFHillMaze(int componentType, int x, int y, int z, int hsize) {
		super(componentType);
		this.coordBaseMode = 0;
		this.hsize = hsize;
		this.centreX = x;
		this.centreY = y;
		this.centreZ = z;

		int radius = cellCount(hsize) * 4;
		this.boundingBox = componentBox(x, y, z,
			-radius, -2, -radius, radius * 2, 6, radius * 2, 0);
	}

	private static int cellCount(int hsize) {
		return switch (hsize) {
			case 2 -> 19;
			case 3 -> 27;
			default -> 11;
		};
	}

	public static int mazeY() {
		return ComponentTFHollowHill.chamberY() - DEPTH_BELOW_CHAMBER;
	}

	@Override
	public int featureType() {
		return this.hsize;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		Random mazeRand = new Random(world.getRandomSeed()
			+ (long) this.boundingBox.minX * this.boundingBox.minZ);

		if (!clip.contains(this.centreX, this.centreY, this.centreZ)) {
			return true;
		}

		new WorldFeatureTFHillMaze(this.hsize)
			.place(world, mazeRand, this.centreX, this.centreY, this.centreZ);
		return true;
	}
}
