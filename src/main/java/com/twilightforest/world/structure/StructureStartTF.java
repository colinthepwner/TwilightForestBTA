package com.twilightforest.world.structure;

import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.chunk.TFBiomeHeights;
import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.TFFeature;
import com.twilightforest.world.layer.TFBiomeIds;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructureStartTF {

	public static final int BASE_Y = TFWorldConstants.SEA_LEVEL + 1;

	public final int feature;

	private final List<StructureComponentTF> components = new ArrayList<>();

	private StructureComponentTF.BoundingBox boundingBox;

	public StructureStartTF(World world, Random rand, int chunkX, int chunkZ, int feature) {
		this.feature = feature;

		int x = (chunkX << 4) + 8;
		int z = (chunkZ << 4) + 8;

		StructureComponentTF first = makeFirstComponent(rand, feature, x, BASE_Y, z);
		if (first != null) {
			this.components.add(first);

			first.buildComponent(first, this.components, rand);
		}

		updateBoundingBox();

		if (first instanceof ComponentTFTowerMain || first instanceof ComponentTFDarkTowerMain) {
			moveToAvgGroundLevel(world, x, z);
		}
	}

	private StructureComponentTF makeFirstComponent(Random rand, int feature, int x, int y, int z) {
		switch (feature) {
			case TFFeature.SMALL_HILL:
			case TFFeature.MEDIUM_HILL:
			case TFFeature.LARGE_HILL:
				return new ComponentTFHollowHill(0, feature, x, y, z);

			case TFFeature.HEDGE_MAZE:
				return new ComponentTFHedgeMaze(0, x, y, z);

			case TFFeature.NAGA_COURTYARD:
				return new ComponentTFNagaCourtyard(0, x, y, z);

			case TFFeature.LICH_TOWER:
				return new ComponentTFTowerMain(0, rand, x, y, z);

			case TFFeature.QUEST_GROVE:
				return new ComponentTFQuestGrove(0, x, y, z);

			case TFFeature.LABYRINTH:

				return new ComponentTFMazeRuins(0, x, y, z);

			case TFFeature.DARK_TOWER:

				return new ComponentTFDarkTowerMain(0, rand, x, y - 1, z);

			default:

				return null;
		}
	}

	public List<StructureComponentTF> getComponents() {
		return this.components;
	}

	public StructureComponentTF.BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public boolean isEmpty() {
		return this.components.isEmpty();
	}

	public void updateBoundingBox() {
		this.boundingBox = null;
		for (StructureComponentTF piece : this.components) {
			if (piece.boundingBox == null) {
				continue;
			}
			if (this.boundingBox == null) {
				this.boundingBox = piece.boundingBox.copy();
			} else {
				this.boundingBox.expandTo(piece.boundingBox);
			}
		}
	}

	public void offsetY(int dy) {
		if (dy == 0) {
			return;
		}
		if (this.boundingBox != null) {
			this.boundingBox.offset(0, dy, 0);
		}
		for (StructureComponentTF piece : this.components) {
			if (piece.boundingBox != null) {
				piece.boundingBox.offset(0, dy, 0);
			}
		}
	}

	protected void moveToAvgGroundLevel(World world, int x, int z) {
		Biome biomeAt = world.getBiomeProvider().getBiome(x, 64, z);
		int layerId = layerIdOf(biomeAt);

		int offY = (int) ((TFBiomeHeights.minHeight(layerId) + TFBiomeHeights.maxHeight(layerId)) * 8.0f);

		if (biomeAt == TFBiomes.DARK_FOREST) {
			offY += 4;
		}

		if (offY > 0) {
			offsetY(offY);
		}
	}

	protected void markAvailableHeight(Random rand, int margin) {
		if (this.boundingBox == null) {
			return;
		}
		int ceiling = TFWorldConstants.SEA_LEVEL + 3 - margin;
		int top = this.boundingBox.getYSize() + 1;
		if (top < ceiling) {
			top += rand.nextInt(ceiling - top);
		}
		offsetY(top - this.boundingBox.maxY);
	}

	private static int layerIdOf(Biome biome) {
		for (int id = TFBiomeIds.MIN; id <= TFBiomeIds.MAX; id++) {
			if (TFBiomes.byLayerId(id) == biome) {
				return id;
			}
		}

		return TFBiomeIds.TWILIGHT_FOREST;
	}
}
