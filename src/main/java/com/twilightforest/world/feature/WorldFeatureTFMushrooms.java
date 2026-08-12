package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.Random;

public class WorldFeatureTFMushrooms extends TFWorldFeature {

	private final int defaultSpecies;

	private final int count;

	public WorldFeatureTFMushrooms(int defaultSpecies, int count) {
		this.defaultSpecies = defaultSpecies;
		this.count = count;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		for (int i = 0; i < this.count; i++) {
			int px = x + rand.nextInt(8) - rand.nextInt(8);
			int py = y + rand.nextInt(4) - rand.nextInt(4);
			int pz = z + rand.nextInt(8) - rand.nextInt(8);

			if (!isAirBlock(world, px, py, pz)) {
				continue;
			}

			if (isLeafy(world, px, py - 1, pz)) {
				continue;
			}

			int species = speciesFor(world, px, py, pz);
			Block<?> block = Blocks.getBlock(species);
			if (block != null && block.canStay(world, new TilePos(px, py, pz))) {
				this.putBlock(px, py, pz, species, true);
			}
		}
		return true;
	}

	private static boolean isLeafy(World world, int x, int y, int z) {
		return getBlockMaterial(world, x, y, z) == Materials.LEAVES;
	}

	private int speciesFor(World world, int x, int y, int z) {
		int below = getBlockId(world, x, y - 1, z);
		if (TFBlocks.MUSHROOM_GIANT_BROWN != null && below == TFBlocks.MUSHROOM_GIANT_BROWN.id()) {
			return Blocks.MUSHROOM_BROWN.id();
		}
		if (TFBlocks.MUSHROOM_GIANT_RED != null && below == TFBlocks.MUSHROOM_GIANT_RED.id()) {
			return Blocks.MUSHROOM_RED.id();
		}
		return this.defaultSpecies;
	}
}
