package com.twilightforest.world.structure;

import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.WorldFeatureTFNagaTemple;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFNagaCourtyard extends StructureComponentTF {

	private static final int RADIUS = 46;

	private final int centreX;
	private final int centreY;
	private final int centreZ;

	public ComponentTFNagaCourtyard(int componentType, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = 0;
		this.centreX = x;
		this.centreY = y;
		this.centreZ = z;
		this.boundingBox = componentBox(x, y, z,
			-RADIUS, -3, -RADIUS, RADIUS * 2, 16, RADIUS * 2, 0);
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
		Random courtyardRand = new Random(world.getRandomSeed()
			+ (long) this.boundingBox.minX * this.boundingBox.minZ);

		if (!clip.contains(this.centreX, this.centreY, this.centreZ)) {
			return true;
		}

		new WorldFeatureTFNagaTemple()
			.place(world, courtyardRand, this.centreX, this.centreY, this.centreZ);
		return true;
	}
}
