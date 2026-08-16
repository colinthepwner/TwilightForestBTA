package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public final class TFStructures {
	private TFStructures() {}

	private static final int MAX_RADIUS = 3;

	public static void generate(World world, Random rand, int chunkX, int chunkZ) {
		int maxY = world.getWorldType().getMaxY(world);
		StructureComponentTF.BoundingBox clip =
			StructureComponentTF.BoundingBox.forChunk(chunkX, chunkZ, maxY);

		for (int dx = -MAX_RADIUS; dx <= MAX_RADIUS; dx++) {
			for (int dz = -MAX_RADIUS; dz <= MAX_RADIUS; dz++) {
				int cx = chunkX + dx;
				int cz = chunkZ + dz;

				int type = TFFeature.featureType(world, cx, cz);
				if (type == TFFeature.NOTHING) {
					continue;
				}

				if (!TFFeature.isStructureEnabled(type)) {
					continue;
				}

				int size = TFFeature.sizeOf(type);
				if (size < 0 || Math.abs(dx) > size || Math.abs(dz) > size) {
					continue;
				}

				long seed = world.getRandomSeed() + cx * 341873128712L + cz * 132897987541L;

				StructureStartTF start =
					new StructureStartTF(world, new Random(seed), cx, cz, type);
				if (start.isEmpty()) {
					continue;
				}

				Random placeRand = new Random(seed);

				for (StructureComponentTF piece : start.getComponents()) {
					if (piece.boundingBox != null && piece.boundingBox.intersects(clip)) {
						piece.addComponentParts(world, placeRand, clip);
					}
				}
			}
		}
	}
}
