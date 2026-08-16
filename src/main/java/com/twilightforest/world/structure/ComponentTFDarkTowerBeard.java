package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFDarkTowerBeard extends StructureComponentTF {

	protected int size;

	protected int height;

	public ComponentTFDarkTowerBeard(int componentType, ComponentTFTowerWing wing) {
		super(componentType);
		setCoordBaseMode(wing.getCoordBaseMode());
		this.size = wing.size;
		this.height = this.size / 2;

		this.boundingBox = new BoundingBox(
			wing.boundingBox.minX,
			wing.boundingBox.minY - this.height,
			wing.boundingBox.minZ,
			wing.boundingBox.maxX,
			wing.boundingBox.minY,
			wing.boundingBox.maxZ);
	}

	@Override
	public int featureType() {
		return TFFeature.DARK_TOWER;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		makeDarkBeard(world, clip, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
		return true;
	}

	protected void makeDarkBeard(World world, BoundingBox clip,
	                             int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

		int frameID = TFBlocks.TOWER_WOOD_ENCASED.id();
		int frameMeta = 0;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (x != minX && x != maxX && z != minZ && z != maxZ) {
					continue;
				}

				int length = Math.min(Math.abs(x - this.height) - 1,
					Math.abs(z - this.height) - 1);
				if (length == this.height - 1) {
					length++;
				}
				if (length == -1) {
					length = 1;
				}

				for (int y = maxY; y >= this.height - length; y--) {
					placeBlock(world, frameID, frameMeta, x, y, z, clip);
				}
			}
		}
	}
}
