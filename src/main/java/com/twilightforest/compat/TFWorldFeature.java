package com.twilightforest.compat;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.pos.TilePos;

import java.util.Random;

public abstract class TFWorldFeature extends WorldFeature {

	protected World worldObj;

	public abstract boolean generate(World world, Random random, int x, int y, int z);

	@Override
	public boolean place(World world, Random random, int x, int y, int z) {
		this.worldObj = world;
		return this.generate(world, random, x, y, z);
	}

	protected boolean putBlockAndMetadata(int dx, int dy, int dz, int blockValue, int metaValue,
	                                      boolean priority) {
		if (priority) {
			this.worldObj.setBlockAndMetadataRaw(dx, dy, dz, blockValue, metaValue);
			return true;
		}
		if (this.worldObj.getBlockId(dx, dy, dz) != 0) {
			return false;
		}
		this.worldObj.setBlockAndMetadataRaw(dx, dy, dz, blockValue, metaValue);
		return true;
	}

	protected boolean putBlock(int dx, int dy, int dz, int blockValue, boolean priority) {
		return this.putBlockAndMetadata(dx, dy, dz, blockValue, 0, priority);
	}

	protected void putBlockAndMetadata(int[] pixel, int blockValue, int metaValue, boolean priority) {
		this.putBlockAndMetadata(pixel[0], pixel[1], pixel[2], blockValue, metaValue, priority);
	}

	protected void putBlock(int[] pixel, int blockValue, boolean priority) {
		this.putBlockAndMetadata(pixel[0], pixel[1], pixel[2], blockValue, 0, priority);
	}

	protected int[] translate(int sx, int sy, int sz, double distance, double angle, double tilt) {
		int[] dest = new int[]{sx, sy, sz};
		double rangle = angle * 2.0 * Math.PI;
		double rtilt = tilt * Math.PI;
		dest[0] = (int) (dest[0] + Math.round(Math.sin(rangle) * Math.sin(rtilt) * distance));
		dest[1] = (int) (dest[1] + Math.round(Math.cos(rtilt) * distance));
		dest[2] = (int) (dest[2] + Math.round(Math.cos(rangle) * Math.sin(rtilt) * distance));
		return dest;
	}

	protected void drawBresehnam(int x1, int y1, int z1, int x2, int y2, int z2, int blockValue,
	                             boolean priority) {
		this.drawBresehnam(x1, y1, z1, x2, y2, z2, blockValue, 0, priority);
	}

	protected void drawBresehnam(int x1, int y1, int z1, int x2, int y2, int z2, int blockValue,
	                             int metaValue, boolean priority) {
		int[] pixel = new int[]{x1, y1, z1};
		int dx = x2 - x1;
		int dy = y2 - y1;
		int dz = z2 - z1;
		int xInc = dx < 0 ? -1 : 1;
		int l = Math.abs(dx);
		int yInc = dy < 0 ? -1 : 1;
		int m = Math.abs(dy);
		int zInc = dz < 0 ? -1 : 1;
		int n = Math.abs(dz);
		int dx2 = l << 1;
		int dy2 = m << 1;
		int dz2 = n << 1;

		if (l >= m && l >= n) {
			int err1 = dy2 - l;
			int err2 = dz2 - l;
			for (int i = 0; i < l; i++) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if (err1 > 0) {
					pixel[1] += yInc;
					err1 -= dx2;
				}
				if (err2 > 0) {
					pixel[2] += zInc;
					err2 -= dx2;
				}
				err1 += dy2;
				err2 += dz2;
				pixel[0] += xInc;
			}
		} else if (m >= l && m >= n) {
			int err1 = dx2 - m;
			int err2 = dz2 - m;
			for (int i = 0; i < m; i++) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if (err1 > 0) {
					pixel[0] += xInc;
					err1 -= dy2;
				}
				if (err2 > 0) {
					pixel[2] += zInc;
					err2 -= dy2;
				}
				err1 += dx2;
				err2 += dz2;
				pixel[1] += yInc;
			}
		} else {
			int err1 = dy2 - n;
			int err2 = dx2 - n;
			for (int i = 0; i < n; i++) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if (err1 > 0) {
					pixel[1] += yInc;
					err1 -= dz2;
				}
				if (err2 > 0) {
					pixel[0] += xInc;
					err2 -= dz2;
				}
				err1 += dy2;
				err2 += dx2;
				pixel[2] += zInc;
			}
		}

		this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
	}

	public void drawCircle(int sx, int sy, int sz, int rad, int blockValue, int metaValue,
	                       boolean priority) {
		for (int dx = 0; dx <= rad; dx++) {
			for (int dz = 0; dz <= rad; dz++) {
				int dist = (int) (Math.max(dx, dz) + Math.min(dx, dz) * 0.5);
				if (dx == 3 && dz == 3) {
					dist = 6;
				}
				if (dist <= rad) {
					this.putBlockAndMetadata(sx + dx, sy, sz + dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx + dx, sy, sz - dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz + dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz - dz, blockValue, metaValue, priority);
				}
			}
		}
	}

	public void drawDiameterCircle(int sx, int sy, int sz, int diam, int block, int meta,
	                               boolean priority) {
		int rad = (diam - 1) / 2;
		if (diam % 2 == 1) {
			this.drawCircle(sx, sy, sz, rad, block, meta, priority);
		} else {
			this.drawCircle(sx, sy, sz, rad, block, meta, priority);
			this.drawCircle(sx + 1, sy, sz, rad, block, meta, priority);
			this.drawCircle(sx, sy, sz + 1, rad, block, meta, priority);
			this.drawCircle(sx + 1, sy, sz + 1, rad, block, meta, priority);
		}
	}

	protected int randStone(Random rand, int howMuch) {
		return rand.nextInt(howMuch) >= 1 ? Blocks.COBBLE_STONE.id() : Blocks.COBBLE_STONE_MOSSY.id();
	}

	protected boolean isAreaClear(World world, Random rand, int x, int y, int z,
	                              int dx, int dy, int dz) {
		boolean flag = true;
		for (int cx = 0; cx < dx; cx++) {
			for (int cz = 0; cz < dy; cz++) {
				Material m = getBlockMaterial(world, x + cx, y - 1, z + cz);
				if (m != Materials.GRASS && m != Materials.DIRT && m != Materials.STONE) {
					flag = false;
				}
				for (int cy = 0; cy < dz; cy++) {
					if (!world.isAirBlock(x + cx, y + cy, z + cz)) {
						flag = false;
					}
				}
			}
		}
		return flag;
	}

	protected static int getBlockId(World world, int x, int y, int z) {
		return world.getBlockId(x, y, z);
	}

	protected static int getBlockMetadata(World world, int x, int y, int z) {
		return world.getBlockData(new TilePos(x, y, z));
	}

	protected static Material getBlockMaterial(World world, int x, int y, int z) {
		return world.getBlockMaterial(new TilePos(x, y, z));
	}

	protected static Block<?> getBlock(World world, int x, int y, int z) {
		return world.getBlockType(new TilePos(x, y, z));
	}

	protected static boolean isAirBlock(World world, int x, int y, int z) {
		return world.isAirBlock(x, y, z);
	}
}
