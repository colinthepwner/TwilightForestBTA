package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerBridge extends ComponentTFTowerWing {

	final int dSize;
	final int dHeight;

	protected ComponentTFTowerBridge(int componentType, int x, int y, int z,
	                                 int destSize, int destHeight, int direction) {
		super(componentType, x, y, z, 3, 3, direction);
		this.dSize = destSize;
		this.dHeight = destHeight;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		makeTowerWing(pieces, rand, 1, 2, 1, 1, this.dSize, this.dHeight, 0);
	}

	public BoundingBox wingBox() {
		int[] dest = offsetTowerCoords(2, 1, 1, this.dSize, this.coordBaseMode);
		return componentBox(dest[0], dest[1], dest[2], 0, 0, 0,
			this.dSize - 1, this.dHeight - 1, this.dSize - 1, this.coordBaseMode);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		for (int x = 0; x < 3; x++) {
			placeBlock(world, FENCE, 0, x, 2, 0, clip);
			placeBlock(world, FENCE, 0, x, 2, 2, clip);
			placeBlock(world, STONE_BRICK, 0, x, 1, 0, clip);
			placeBlock(world, STONE_BRICK, 0, x, 1, 2, clip);
			placeBlock(world, STONE_BRICK, 0, x, 0, 0, clip);
			placeBlock(world, STONE_BRICK, 0, x, 0, 1, clip);
			placeBlock(world, STONE_BRICK, 0, x, 0, 2, clip);
			placeBlock(world, STONE_BRICK, 0, x, -1, 1, clip);
		}
		return true;
	}
}
