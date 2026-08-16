package com.twilightforest.world.structure;

import com.twilightforest.world.chunk.TFWorldConstants;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerMain extends ComponentTFTowerWing {

	public ComponentTFTowerMain(int componentType, Random rand, int x, int y, int z) {
		super(componentType, x, y, z, 15,
			45 + rand.nextInt(TFWorldConstants.WORLD_HEIGHT - TFWorldConstants.SEA_LEVEL - 56), 0);
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		makeARoof(parent, pieces, rand);

		for (int i = 0; i < 4; i++) {
			int[] dest = getValidOpening(rand, i);
			int childHeight = Math.min(21 + rand.nextInt(10), this.height - dest[1] - 3);
			if (!makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 9, childHeight, i)) {
				makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 7, childHeight, i);
			}
		}

		for (int i = 0; i < 4; i++) {
			int[] dest = getValidOpening(rand, i);
			int childHeight = Math.min(7 + rand.nextInt(6), this.height - dest[1] - 3);
			if (!makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 5, childHeight, i)) {
				makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 3, childHeight, i);
			}
		}

		for (int i = 0; i < 4; i++) {
			int[] dest = getOutbuildingOpening(rand, i);
			int childHeight = 11 + rand.nextInt(10);
			int childSize = 7 + rand.nextInt(2) * 2;
			makeTowerOutbuilding(pieces, rand, 1, dest[0], dest[1], dest[2],
				childSize, childHeight, i);
		}

		for (int i = 0; i < 16; i++) {
			int[] dest = getValidOpening(rand, i % 4);
			int childHeight = 6 + rand.nextInt(5);
			if (rand.nextInt(3) == 0
				|| !makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 5, childHeight, i % 4)) {
				makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2], 3, childHeight, i % 4);
			}
		}
	}

	public int[] getOutbuildingOpening(Random rand, int rotation) {
		int rx = 0;
		int ry = 1;
		int rz = 0;

		switch (rotation) {
			case 0 -> { rx = this.size - 1; rz = 6 + rand.nextInt(8); }
			case 1 -> { rx = 1 + rand.nextInt(11); rz = this.size - 1; }
			case 2 -> { rx = 0; rz = 1 + rand.nextInt(8); }
			case 3 -> { rx = 3 + rand.nextInt(11); rz = 0; }
			default -> { }
		}

		return new int[]{rx, ry, rz};
	}

	public boolean makeTowerOutbuilding(List<StructureComponentTF> pieces, Random rand, int index,
	                                    int x, int y, int z, int wingSize, int wingHeight,
	                                    int rotation) {
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, wingSize, direction);

		ComponentTFTowerOutbuilding outbuilding = new ComponentTFTowerOutbuilding(
			index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

		StructureComponentTF hit = findIntersecting(pieces, outbuilding.boundingBox);
		if (hit != null && hit != this) {
			return false;
		}

		pieces.add(outbuilding);
		outbuilding.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		fillWithRandomizedBlocks(world, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1, false, rand, TOWER_STONE);
		makeStairs(world, rand, clip);
		makeOpenings(world, clip);
		decorateThisTower(world, rand, clip);
		return true;
	}
}
