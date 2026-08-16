package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBridge extends ComponentTFTowerWing {

	int dSize;
	int dHeight;

	protected ComponentTFDarkTowerBridge(int componentType, int x, int y, int z,
	                                     int destSize, int destHeight, int direction) {

		super(componentType, x, y, z, 5, 5, direction);
		this.dSize = destSize;
		this.dHeight = destHeight;
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

		makeTowerWing(pieces, rand, componentType(), 4, 1, 2, this.dSize, this.dHeight, 0);
	}

	@Override
	public boolean makeTowerWing(List<StructureComponentTF> pieces, Random rand, int index,
	                             int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		if (wingHeight < 6) {
			return false;
		}

		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, wingSize, direction);

		if (dx[1] + wingHeight > 255) {
			return false;
		}

		ComponentTFDarkTowerWing wing = new ComponentTFDarkTowerWing(
			index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

		StructureComponentTF hit = findIntersecting(pieces, wing.boundingBox);
		if (hit != null && hit != this) {
			return false;
		}

		pieces.add(wing);
		wing.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		fillWithBlocks(world, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1,
			this.deco.blockID, this.deco.blockMeta, 0, 0, false);

		for (int x = 0; x < this.size; x++) {
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, 0, 0, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, this.height - 1, 0, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, 0, this.size - 1, clip);
			placeBlock(world, this.deco.accentID, this.deco.accentMeta,
				x, this.height - 1, this.size - 1, clip);
		}

		nullifySkyLightForBoundingBox(world);

		fillWithBlocks(world, clip, 0, 1, 1,
			this.size - 1, this.height - 2, this.size - 2, 0, 0, 0, 0, false);
		return true;
	}

	public BoundingBox wingBox() {
		int[] dest = offsetTowerCoords(4, 1, 2, this.dSize, this.coordBaseMode);
		return componentBox(dest[0], dest[1], dest[2], 0, 0, 0,
			this.dSize - 1, this.dHeight - 1, this.dSize - 1, this.coordBaseMode);
	}
}
