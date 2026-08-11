package com.twilightforest.world.chunk;

import java.util.Random;

public final class TFHollowHills {
	private TFHollowHills() {}

	private static final int LATTICE = 7;

	private static final int LATTICE_OFFSET = 4;

	private static final int MAX_SIZE = 3;

	public static int hillSize(int cx, int cz, long seed) {
		Random hillRNG = new Random(seed + cx * 25117L + cz * 151121L);
		int hn = hillRNG.nextInt();
		int hs = -1;

		if ((cx % LATTICE == LATTICE_OFFSET || cx % LATTICE == -LATTICE_OFFSET)
			&& (cz % LATTICE == LATTICE_OFFSET || cz % LATTICE == -LATTICE_OFFSET)) {
			hs = Math.abs(hn % 6);
			if (hs == 0 || hs > MAX_SIZE) {
				hs = -1;
			}
		}

		return hs;
	}

	public static boolean isHollowHill(int cx, int cz, long seed) {
		return hillSize(cx, cz, seed) > 0;
	}

	public static boolean nearHollowHill(int cx, int cz, long seed) {
		for (int rad = 1; rad <= MAX_SIZE; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (hillSize(x + cx, z + cz, seed) == rad) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int[] nearestHillCenter(int cx, int cz, long seed) {
		for (int rad = 1; rad <= MAX_SIZE; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (hillSize(x + cx, z + cz, seed) == rad) {
						return new int[]{x * 16 + 8, z * 16 + 8};
					}
				}
			}
		}
		return new int[]{0, 0};
	}

	public static int nearestHillSize(int cx, int cz, long seed) {
		for (int rad = 1; rad <= MAX_SIZE; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (hillSize(x + cx, z + cz, seed) == rad) {
						return rad;
					}
				}
			}
		}
		return -1;
	}
}
