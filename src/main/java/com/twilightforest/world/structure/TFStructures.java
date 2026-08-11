package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.ArrayList;
import java.util.List;
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

				int size = TFFeature.featureSize(world, cx, cz);
				if (size < 0 || Math.abs(dx) > size || Math.abs(dz) > size) {
					continue;
				}

				List<StructureComponentTF> pieces = start(world, cx, cz, type);
				if (pieces.isEmpty()) {
					continue;
				}

				Random structureRand = new Random(world.getRandomSeed() + cx * 341873128712L
					+ cz * 132897987541L);

				for (StructureComponentTF piece : pieces) {
					if (piece.boundingBox.intersects(clip)) {
						piece.addComponentParts(world, structureRand, clip);
					}
				}
			}
		}
	}

	private static List<StructureComponentTF> start(World world, int cx, int cz, int type) {
		List<StructureComponentTF> pieces = new ArrayList<>();
		Random rand = new Random(world.getRandomSeed() + cx * 341873128712L + cz * 132897987541L);

		if (type == TFFeature.SMALL_HILL || type == TFFeature.MEDIUM_HILL
			|| type == TFFeature.LARGE_HILL) {
			int x = (cx << 4) + 8;
			int z = (cz << 4) + 8;
			int y = ComponentTFHollowHill.chamberY();
			ComponentTFHollowHill hill = new ComponentTFHollowHill(0, type, x, y, z);
			pieces.add(hill);
			hill.buildComponent(hill, pieces, rand);
		}

		return pieces;
	}
}
