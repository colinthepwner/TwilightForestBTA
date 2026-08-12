package com.twilightforest.world.structure;

public class ComponentTFTowerRoofPointyOverhang extends ComponentTFTowerRoofPointy {

	public ComponentTFTowerRoofPointyOverhang(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		this.coordBaseMode = wing.coordBaseMode;
		this.size = wing.size + 2;
		this.height = this.size;
		makeOverhangBB(wing);
	}
}
