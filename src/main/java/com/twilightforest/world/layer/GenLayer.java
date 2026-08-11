package com.twilightforest.world.layer;

public abstract class GenLayer {

	protected GenLayer parent;

	private long worldGenSeed;

	private long chunkSeed;

	private final long baseSeed;

	protected GenLayer(long baseSeed) {
		long seed = baseSeed;
		seed *= seed * 6364136223846793005L + 1442695040888963407L;
		seed += baseSeed;
		seed *= seed * 6364136223846793005L + 1442695040888963407L;
		seed += baseSeed;
		seed *= seed * 6364136223846793005L + 1442695040888963407L;
		seed += baseSeed;
		this.baseSeed = seed;
	}

	public void initWorldGenSeed(long seed) {
		this.worldGenSeed = seed;
		if (this.parent != null) {
			this.parent.initWorldGenSeed(seed);
		}
		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
		this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
		this.worldGenSeed += this.baseSeed;
	}

	public void initChunkSeed(long x, long z) {
		this.chunkSeed = this.worldGenSeed;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += x;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += z;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += x;
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += z;
	}

	protected int nextInt(int bound) {
		int value = (int) ((this.chunkSeed >> 24) % bound);
		if (value < 0) {
			value += bound;
		}
		this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
		this.chunkSeed += this.worldGenSeed;
		return value;
	}

	public abstract int[] getInts(int x, int z, int width, int height);
}
