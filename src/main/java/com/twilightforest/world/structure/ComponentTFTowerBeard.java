package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFTowerBeard extends StructureComponentTF {

	private final int size;
	private final int height;

	public ComponentTFTowerBeard(int componentType, ComponentTFTowerWing wing) {
		super(componentType);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size - 2;
		this.height = this.size / 2;

		this.boundingBox = new BoundingBox(
			wing.boundingBox.minX + 1,
			wing.boundingBox.minY - this.height - 1,
			wing.boundingBox.minZ + 1,
			wing.boundingBox.maxX - 1,
			wing.boundingBox.minY - 1,
			wing.boundingBox.maxZ - 1);
	}

	@Override
	public int featureType() {
		return com.twilightforest.world.feature.TFFeature.LICH_TOWER;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int stoneBrick = Blocks.BRICK_STONE_POLISHED.id();

		for (int y = 0; y <= this.height; y++) {
			int min = y;
			int max = this.size - y - 1;

			for (int x = y; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					placeBlock(world, stoneBrick, 0, x, this.height - y, z, clip);
				}
			}
		}
		return true;
	}
}
