package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBalcony extends ComponentTFTowerWing {

	protected ComponentTFDarkTowerBalcony(int componentType, int x, int y, int z, int direction) {
		super(componentType, x, y, z, 5, 5, direction);
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

		fillWithBlocks(world, clip, 0, 0, 0, 2, 0, 4,
			this.deco.accentID, this.deco.accentMeta, 0, 0, false);

		fillWithBlocks(world, clip, 0, 0, 1, 1, 0, 3,
			this.deco.blockID, this.deco.blockMeta, 0, 0, false);

		fillWithBlocks(world, clip, 0, 1, 0, 2, 1, 4,
			this.deco.fenceID, this.deco.fenceMeta, 0, 0, false);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, 2, 1, 0, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, 2, 1, 4, clip);

		fillWithBlocks(world, clip, 0, 1, 1, 1, 1, 3, 0, 0, 0, 0, false);
		return true;
	}
}
