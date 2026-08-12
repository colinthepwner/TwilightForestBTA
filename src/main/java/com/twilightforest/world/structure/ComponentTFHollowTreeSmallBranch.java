package com.twilightforest.world.structure;

import java.util.List;
import java.util.Random;

public class ComponentTFHollowTreeSmallBranch extends ComponentTFHollowTreeMedBranch {

	protected ComponentTFHollowTreeSmallBranch(int componentType, int sx, int sy, int sz,
	                                           double length, double angle, double tilt,
	                                           boolean leafy) {
		super(componentType, sx, sy, sz, length, angle, tilt, leafy);
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		if (!this.leafy) {
			return;
		}
		int leafRad = rand.nextInt(2) + 1;
		addLeafSphere(pieces, rand, componentType() + 1, this.dest, leafRad);
	}
}
