package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerRoof extends ComponentTFTowerRoof {

	public ComponentTFDarkTowerRoof(int componentType, ComponentTFTowerWing wing) {
		super(componentType, wing);
		setCoordBaseMode(wing.getCoordBaseMode());
		this.size = wing.size;
		this.height = 12;
		makeCapBB(wing);
		this.spawnListIndex = 1;
	}

	@Override
	public int featureType() {
		return TFFeature.DARK_TOWER;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		if (parent != null) {
			this.deco = parent.deco;
		}
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		for (int x = 0; x <= this.size - 1; x++) {
			for (int z = 0; z <= this.size - 1; z++) {
				if (x != 0 && x != this.size - 1 && z != 0 && z != this.size - 1) {
					continue;
				}
				placeBlock(world, this.deco.fenceID, this.deco.fenceMeta, x, 1, z, clip);
			}
		}

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, 0, 1, 0, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, this.size - 1, 1, 0, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, 0, 1, this.size - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta,
			this.size - 1, 1, this.size - 1, clip);
		return true;
	}
}
