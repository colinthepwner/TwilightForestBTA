package com.twilightforest.world.structure;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerOutbuilding extends ComponentTFTowerWing {

	protected ComponentTFTowerOutbuilding(int componentType, int x, int y, int z,
	                                      int size, int height, int direction) {
		super(componentType, x, y, z, size, height, direction);
	}

	@Override
	public void makeABeard(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                       Random rand) {

	}

	@Override
	public boolean makeTowerWing(List<StructureComponentTF> pieces, Random rand, int index,
	                             int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		if (y <= 7) {
			return false;
		}
		return super.makeTowerWing(pieces, rand, index, x, y, z, wingSize, wingHeight, rotation);
	}
}
