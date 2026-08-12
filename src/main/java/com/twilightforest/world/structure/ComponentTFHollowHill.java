package com.twilightforest.world.structure;

import com.twilightforest.TwilightForest;
import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.WorldFeatureTFCaveStalactite;
import net.minecraft.core.block.Blocks;
import com.twilightforest.world.treasure.TFTreasure;
import com.twilightforest.world.treasure.TFTreasureTable;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFHollowHill extends StructureComponentTF {

	private static final TFTreasureTable[] HILL_TABLES = {
		TFTreasure.HILL_1, TFTreasure.HILL_1, TFTreasure.HILL_2, TFTreasure.HILL_3,
	};

	private final int hsize;
	private final int radius;

	public ComponentTFHollowHill(int componentType, int size, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = 0;
		this.hsize = size;
		this.radius = (size * 2 + 1) * 8 - 6;
		this.boundingBox = componentBox(x, y, z,
			-this.radius, -3, -this.radius, this.radius * 2, 10, this.radius * 2, 0);
	}

	@Override
	public int featureType() {
		return this.hsize;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int area = (int) (Math.PI * this.radius * this.radius);
		int stalactites = area / 16;

		int[] spawnerCounts = {0, 3, 9, 18};
		int[] chestCounts = {0, 2, 6, 12};
		int spawners = spawnerCounts[this.hsize];
		int chests = chestCounts[this.hsize];

		for (int i = 0; i < spawners; i++) {
			int[] dest = coordsInHill(rand);
			placeSpawner(world, rand, dest[0], rand.nextInt(4), dest[1], mobId(rand), clip);
		}

		for (int i = 0; i < chests; i++) {
			int[] dest = coordsInHill(rand);
			placeTreasureChest(world, rand, dest[0], 0, dest[1], clip);
		}

		for (int i = 0; i < stalactites; i++) {
			int[] dest = coordsInHill(rand);
			placeOreStalactite(world, dest[0], 1, dest[1], clip);
		}
		for (int i = 0; i < stalactites; i++) {
			int[] dest = coordsInHill(rand);
			placeStoneStalactite(world, 1.0, true, dest[0], 1, dest[1], clip);
		}
		for (int i = 0; i < stalactites; i++) {
			int[] dest = coordsInHill(rand);
			placeStoneStalactite(world, 0.7, false, dest[0], 1, dest[1], clip);
		}

		return true;
	}

	private void placeTreasureChest(World world, Random rand, int x, int y, int z, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz) || world.getBlockId(wx, wy, wz) == Blocks.CHEST_PLANKS_OAK.id()) {
			return;
		}

		TFTreasure.place(world, rand, wx, wy, wz, HILL_TABLES[this.hsize]);
	}

	private void placeOreStalactite(World world, int x, int y, int z, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		Random stalRNG = new Random(world.getRandomSeed() + (long) wx * wz);
		WorldFeatureTFCaveStalactite.makeRandomOreStalactite(stalRNG, this.hsize)
			.place(world, stalRNG, wx, wy, wz);
	}

	private void placeStoneStalactite(World world, double length, boolean hanging,
	                                  int x, int y, int z, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		Random stalRNG = new Random(world.getRandomSeed() + (long) wx * wz);
		new WorldFeatureTFCaveStalactite(Blocks.STONE.id(), stalRNG.nextDouble() * length, hanging)
			.place(world, stalRNG, wx, wy, wz);
	}

	private boolean isInHill(int cx, int cz) {
		int dx = this.radius - cx;
		int dz = this.radius - cz;
		return (int) Math.sqrt((double) dx * dx + (double) dz * dz) < this.radius;
	}

	private int[] coordsInHill(Random rand) {
		int rx;
		int rz;
		do {
			rx = rand.nextInt(2 * this.radius);
			rz = rand.nextInt(2 * this.radius);
		} while (!isInHill(rx, rz));
		return new int[]{rx, rz};
	}

	private static final String SWARM_SPIDER = TwilightForest.MOD_ID + ":swarmspider";
	private static final String REDCAP = TwilightForest.MOD_ID + ":redcap";
	private static final String WRAITH = TwilightForest.MOD_ID + ":wraith";

	private static final String SILVERFISH_STANDIN = "minecraft:scorpion";
	private static final String CAVE_SPIDER_STANDIN = "minecraft:scorpion";
	private static final String ENDERMAN_STANDIN = "minecraft:zombie_armored";

	private String mobId(Random rand) {
		switch (this.hsize) {
			case 1: return level1Mob(rand);
			case 2: return level2Mob(rand);
			case 3: return level3Mob(rand);
			default: return "minecraft:spider";
		}
	}

	private String level1Mob(Random rand) {
		return switch (rand.nextInt(10)) {
			case 0, 1, 2 -> SWARM_SPIDER;
			case 3, 4, 5 -> "minecraft:spider";
			case 6, 7 -> "minecraft:zombie";
			case 8 -> SILVERFISH_STANDIN;
			default -> REDCAP;
		};
	}

	private String level2Mob(Random rand) {
		return switch (rand.nextInt(10)) {
			case 0, 1, 2 -> REDCAP;
			case 3, 4, 5 -> "minecraft:zombie";
			case 6, 7 -> "minecraft:skeleton";
			case 8 -> SWARM_SPIDER;
			default -> CAVE_SPIDER_STANDIN;
		};
	}

	private String level3Mob(Random rand) {
		return switch (rand.nextInt(11)) {
			case 0, 1, 2 -> ENDERMAN_STANDIN;
			case 3, 4, 5 -> "minecraft:skeleton";
			case 6, 7, 8 -> CAVE_SPIDER_STANDIN;
			case 9 -> "minecraft:creeper";
			default -> WRAITH;
		};
	}

	public static int chamberY() {
		return TFWorldConstants.SEA_LEVEL + 1;
	}
}
