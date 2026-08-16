package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBossTrap extends ComponentTFDarkTowerWing {

	private static final int META_GHASTTRAP_INACTIVE = 10;

	private static final int REDSTONE = Blocks.WIRE_REDSTONE.id();
	private static final int PRESSURE_PLATE = Blocks.PRESSURE_PLATE_PLANKS_OAK.id();

	protected ComponentTFDarkTowerBossTrap(int componentType, int x, int y, int z,
	                                       int pSize, int pHeight, int direction) {
		super(componentType, x, y, z, pSize, pHeight, direction);
		this.spawnListIndex = -1;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		if (parent != null) {
			this.deco = parent.deco;
		}

		addOpening(0, 1, this.size / 2, 2);
		makeABeard(parent, pieces, rand);

		for (int i = 0; i < 4; i++) {

			if (i == 2 || rand.nextBoolean()) {
				continue;
			}

			int[] dest = getValidOpening(rand, i);

			dest[1] = 1;

			makeTowerBalcony(pieces, rand, componentType(), dest[0], dest[1], dest[2], i);
		}
	}

	@Override
	public void makeARoof(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                      Random rand) {
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		Random decoRNG = new Random(
			(world.getRandomSeed() + (long) (this.boundingBox.minX * 321534781))
				^ (long) (this.boundingBox.minZ * 756839));

		makeEncasedWalls(world, rand, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1);

		fillWithBlocks(world, clip, 1, 1, 1,
			this.size - 2, this.height - 2, this.size - 2, 0, 0, 0, 0, false);

		makeOpenings(world, clip);
		addBossTrapFloors(world, decoRNG, clip, 4, this.height - 1);

		destroyTower(world, decoRNG, 5, this.height + 2, 5, 4, clip);
		destroyTower(world, decoRNG, 0, this.height, 0, 3, clip);
		destroyTower(world, decoRNG, 0, this.height, 8, 4, clip);
		destroyTower(world, decoRNG, 5, 6, 5, 2, clip);

		fillWithBlocks(world, clip, 1, 0, 1, this.size / 2, 0, this.size - 2,
			this.deco.blockID, this.deco.blockMeta, 0, 0, false);
		fillWithBlocks(world, clip, 1, 1, 1, this.size / 2, 1, this.size - 2, 0, 0, 0, 0, false);

		placeBlock(world, TFBlocks.TOWER_DEVICE.id(), META_GHASTTRAP_INACTIVE, 5, 1, 5, clip);
		placeBlock(world, REDSTONE, 0, 5, 1, 6, clip);
		placeBlock(world, REDSTONE, 0, 5, 1, 7, clip);
		placeBlock(world, REDSTONE, 0, 5, 1, 8, clip);
		placeBlock(world, REDSTONE, 0, 4, 1, 8, clip);
		placeBlock(world, REDSTONE, 0, 3, 1, 8, clip);
		placeBlock(world, PRESSURE_PLATE, 0, 2, 1, 8, clip);
		return true;
	}

	protected void addBossTrapFloors(World world, Random rand, BoundingBox clip, int bottom, int top) {
		makeFullFloor(world, clip, 3, 4, 4);

		addStairsDown(world, clip, 3, 4, this.size - 2, 4);
		addStairsDown(world, clip, 3, 4, this.size - 3, 4);

		addStairsDown(world, clip, 1, this.height - 1, this.size - 2, 4);
		addStairsDown(world, clip, 1, this.height - 1, this.size - 3, 4);
	}
}
