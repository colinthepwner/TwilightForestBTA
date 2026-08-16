package com.twilightforest.world.structure;

import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerEntrance extends ComponentTFDarkTowerWing {

	protected ComponentTFDarkTowerEntrance(int componentType, int x, int y, int z,
	                                       int pSize, int pHeight, int direction) {
		super(componentType, x, y, z, pSize, pHeight, direction);
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		super.buildComponent(parent, pieces, rand);
		addOpening(this.size / 2, 1, 0, 1, EnumDarkTowerDoor.REAPPEARING);
		addOpening(this.size / 2, 1, this.size - 1, 3, EnumDarkTowerDoor.REAPPEARING);
	}

	@Override
	public void makeABeard(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                       Random rand) {
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		makeEncasedWalls(world, rand, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1);

		for (int x = 0; x < this.size; x++) {
			for (int z = 0; z < this.size; z++) {
				fillDownwards(world, this.deco.accentID, this.deco.accentMeta, x, -1, z, clip);
			}
		}

		fillWithBlocks(world, clip, 1, 1, 1,
			this.size - 2, this.height - 2, this.size - 2, 0, 0, 0, 0, false);

		nullifySkyLightForBoundingBox(world);

		makeOpenings(world, clip);
		return true;
	}

	private void fillDownwards(World world, int blockId, int meta, int x, int y, int z,
	                           BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);

		if (!clip.contains(wx, wy, wz)) {
			return;
		}

		while (wy > 1 && (world.isAirBlock(wx, wy, wz)
			|| world.getBlockMaterial(new TilePos(wx, wy, wz)).isLiquid())) {
			world.setBlockAndMetadataRaw(wx, wy, wz, blockId, meta);
			wy--;
		}
	}
}
