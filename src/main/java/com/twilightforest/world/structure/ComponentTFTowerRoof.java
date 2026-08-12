package com.twilightforest.world.structure;

import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public abstract class ComponentTFTowerRoof extends StructureComponentTF {

	protected int size;

	protected int height;

	protected ComponentTFTowerRoof(int componentType, ComponentTFTowerWing wing) {
		super(componentType);

	}

	@Override
	public int featureType() {
		return com.twilightforest.world.feature.TFFeature.LICH_TOWER;
	}

	protected void makeAttachedOverhangBB(ComponentTFTowerWing wing) {
		BoundingBox w = wing.boundingBox;
		switch (this.coordBaseMode) {
			case 0 -> this.boundingBox = new BoundingBox(
				w.minX, w.maxY, w.minZ - 1, w.maxX + 1, w.maxY + this.height - 1, w.maxZ + 1);
			case 1 -> this.boundingBox = new BoundingBox(
				w.minX - 1, w.maxY, w.minZ, w.maxX + 1, w.maxY + this.height - 1, w.maxZ + 1);
			case 2 -> this.boundingBox = new BoundingBox(
				w.minX - 1, w.maxY, w.minZ - 1, w.maxX, w.maxY + this.height - 1, w.maxZ + 1);
			case 3 -> this.boundingBox = new BoundingBox(
				w.minX - 1, w.maxY, w.minZ - 1, w.maxX + 1, w.maxY + this.height - 1, w.maxZ);
			default -> { }
		}
	}

	protected void makeCapBB(ComponentTFTowerWing wing) {
		BoundingBox w = wing.boundingBox;
		this.boundingBox = new BoundingBox(
			w.minX, w.maxY, w.minZ, w.maxX, w.maxY + this.height, w.maxZ);
	}

	protected void makeOverhangBB(ComponentTFTowerWing wing) {
		BoundingBox w = wing.boundingBox;
		this.boundingBox = new BoundingBox(
			w.minX - 1, w.maxY, w.minZ - 1, w.maxX + 1, w.maxY + this.height - 1, w.maxZ + 1);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return false;
	}

	public boolean fits(ComponentTFTowerWing parent, List<StructureComponentTF> pieces,
	                    Random rand) {
		return findIntersecting(pieces, this.boundingBox) == parent;
	}
}
