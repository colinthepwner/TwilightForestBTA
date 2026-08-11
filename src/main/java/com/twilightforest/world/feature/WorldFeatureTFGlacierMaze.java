package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFGlacierMaze extends TFWorldFeature {

	private final int size;

	public WorldFeatureTFGlacierMaze(int size) {
		this.size = size;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		int sx = x - 7 - this.size * 16;
		int sz = z - 7 - this.size * 16;

		TFMaze maze = new TFMaze(15, 15);
		maze.setSeed(rand.nextLong());
		maze.oddBias = 2;
		maze.wallBlockId = Blocks.ICE.id();
		maze.torchBlockId = Blocks.ICE.id();
		maze.type = 5;
		maze.tall = 3;

		maze.generateRecursiveBacktracker(0, 0);
		maze.add4Exits();
		maze.carveToWorld(world, sx, y, sz);
		return true;
	}
}
