package com.twilightforest.world.structure;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerMainBridge extends ComponentTFDarkTowerBridge {

	protected ComponentTFDarkTowerMainBridge(int componentType, int x, int y, int z,
	                                         int destSize, int destHeight, int direction) {

		super(componentType, x, y, z, 15, destHeight, direction);
	}

	@Override
	public boolean makeTowerWing(List<StructureComponentTF> pieces, Random rand, int index,
	                             int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 19, direction);

		ComponentTFDarkTowerMain wing =
			new ComponentTFDarkTowerMain(null, rand, index, dx[0], dx[1], dx[2], direction);

		pieces.add(wing);
		wing.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}
}
