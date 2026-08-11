package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFCaveStalactite extends TFWorldFeature {

	public final double size;
	public final int bType;
	public final boolean hang;
	public final int dir;

	public WorldFeatureTFCaveStalactite(int blockType, double sizeFactor, boolean down) {
		this.bType = blockType;
		this.size = sizeFactor;
		this.hang = down;
		this.dir = this.hang ? -1 : 1;
	}

	public static WorldFeatureTFCaveStalactite makeRandomOreStalactite(Random rand, int caveSize) {
		if (caveSize >= 3) {
			int s3 = rand.nextInt(6);
			if (s3 == 0) {
				return new WorldFeatureTFCaveStalactite(Blocks.ORE_DIAMOND_STONE.id(), rand.nextDouble() * 0.5, true);
			}
			if (s3 == 1) {
				return new WorldFeatureTFCaveStalactite(Blocks.ORE_LAPIS_STONE.id(), rand.nextDouble() * 0.8, true);
			}
		}

		if (caveSize >= 2) {
			int s2 = rand.nextInt(6);
			if (s2 == 0) {
				return new WorldFeatureTFCaveStalactite(Blocks.ORE_GOLD_STONE.id(), rand.nextDouble() * 0.6, true);
			}
			if (s2 == 1 || s2 == 2) {
				return new WorldFeatureTFCaveStalactite(Blocks.ORE_REDSTONE_STONE.id(), rand.nextDouble() * 0.8, true);
			}
		}

		int s1 = rand.nextInt(5);
		if (s1 == 0 || s1 == 1) {
			return new WorldFeatureTFCaveStalactite(Blocks.ORE_IRON_STONE.id(), rand.nextDouble() * 0.7, true);
		}
		if (s1 == 2 || s1 == 3) {
			return new WorldFeatureTFCaveStalactite(Blocks.ORE_COAL_STONE.id(), rand.nextDouble() * 0.8, true);
		}
		return new WorldFeatureTFCaveStalactite(Blocks.GLOWSTONE.id(), rand.nextDouble() * 0.5, true);
	}

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;

		int maxY = world.getWorldType().getMaxY(world);
		int ceiling = maxY + 2;
		int floor = -1;

		for (int ty = y; ty <= maxY; ty++) {
			Material m = getBlockMaterial(world, x, ty, z);
			if (m == Materials.AIR) {
				continue;
			}
			if (m != Materials.DIRT && m != Materials.STONE) {
				return false;
			}
			ceiling = ty;
			break;
		}

		if (ceiling == maxY + 2) {
			return false;
		}

		for (int ty = y; ty > 4; ty--) {
			Material m = getBlockMaterial(world, x, ty, z);
			if (m == Materials.AIR) {
				continue;
			}
			boolean solidFloor = m == Materials.DIRT || m == Materials.STONE;
			boolean liquidFloor = !this.hang && (m == Materials.WATER || m == Materials.LAVA);
			if (!solidFloor && !liquidFloor) {
				return false;
			}
			floor = ty;
			break;
		}

		int length = (int) ((ceiling - floor) * this.size);
		return this.makeSpike(random, x, this.hang ? ceiling : floor, z, length);
	}

	public boolean makeSpike(Random random, int x, int y, int z, int length) {
		int dw = (int) (length / 4.5);

		for (int dx = -dw; dx <= dw; dx++) {
			for (int dz = -dw; dz <= dw; dz++) {
				int ax = Math.abs(dx);
				int az = Math.abs(dz);
				int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);

				int dl = 0;
				if (dist == 0) {
					dl = length;
				}
				if (dist > 0) {
					dl = random.nextInt((int) (length / (dist + 0.25)));
				}

				int dy = 0;
				while (dy != dl * this.dir) {
					this.putBlock(x + dx, y + dy, z + dz, this.bType, false);
					dy += this.dir;
				}
			}
		}

		return true;
	}
}
