package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class TFMaze {

	static final int OUT_OF_BOUNDS = Integer.MIN_VALUE;

	static final int ROOM = 5;

	private final int width;
	private final int depth;
	private final int rawWidth;
	private final int rawDepth;
	private final int[] storage;

	public int oddBias = 3;

	public int evenBias = 1;

	public int tall = 3;

	public int roots = 0;

	public int type;

	public int wallBlockId = TFBlocks.MAZESTONE_MOSSY.id();
	public int rootBlockId = TFBlocks.MAZESTONE.id();
	public int torchBlockId = Blocks.TORCH_COAL.id();
	public int torchBlockMeta = 0;

	public int worldX;
	public int worldY;
	public int worldZ;

	private final Random rand = new Random();

	public TFMaze(int cellsWidth, int cellsDepth) {
		this.width = cellsWidth;
		this.depth = cellsDepth;
		this.rawWidth = this.width * 2 + 1;
		this.rawDepth = this.depth * 2 + 1;
		this.storage = new int[this.rawWidth * this.rawDepth];
	}

	public void setSeed(long seed) {
		this.rand.setSeed(seed);
	}

	public int getCell(int x, int z) {
		return getRaw(x * 2 + 1, z * 2 + 1);
	}

	public void putCell(int x, int z, int value) {
		putRaw(x * 2 + 1, z * 2 + 1, value);
	}

	public boolean cellEquals(int x, int z, int value) {
		return getCell(x, z) == value;
	}

	public int getWall(int sx, int sz, int dx, int dz) {
		if (dx == sx + 1 && dz == sz) return getRaw(sx * 2 + 2, sz * 2 + 1);
		if (dx == sx - 1 && dz == sz) return getRaw(sx * 2, sz * 2 + 1);
		if (dx == sx && dz == sz + 1) return getRaw(sx * 2 + 1, sz * 2 + 2);
		if (dx == sx && dz == sz - 1) return getRaw(sx * 2 + 1, sz * 2);
		return OUT_OF_BOUNDS;
	}

	public void putWall(int sx, int sz, int dx, int dz, int value) {
		if (dx == sx + 1 && dz == sz) putRaw(sx * 2 + 2, sz * 2 + 1, value);
		if (dx == sx - 1 && dz == sz) putRaw(sx * 2, sz * 2 + 1, value);
		if (dx == sx && dz == sz + 1) putRaw(sx * 2 + 1, sz * 2 + 2, value);
		if (dx == sx && dz == sz - 1) putRaw(sx * 2 + 1, sz * 2, value);
	}

	public boolean isWall(int sx, int sz, int dx, int dz) {
		return getWall(sx, sz, dx, dz) == 0;
	}

	protected void putRaw(int rawX, int rawZ, int value) {
		if (rawX >= 0 && rawX < this.rawWidth && rawZ >= 0 && rawZ < this.rawDepth) {
			this.storage[rawZ * this.rawWidth + rawX] = value;
		}
	}

	protected int getRaw(int rawX, int rawZ) {
		if (rawX < 0 || rawX >= this.rawWidth || rawZ < 0 || rawZ >= this.rawDepth) {
			return OUT_OF_BOUNDS;
		}
		return this.storage[rawZ * this.rawWidth + rawX];
	}

	private static boolean isEven(int n) {
		return n % 2 == 0;
	}

	public void generateRecursiveBacktracker(int sx, int sz) {
		rbGen(sx, sz);
	}

	private void rbGen(int sx, int sz) {
		putCell(sx, sz, 1);

		int unvisited = 0;
		if (cellEquals(sx + 1, sz, 0)) unvisited++;
		if (cellEquals(sx - 1, sz, 0)) unvisited++;
		if (cellEquals(sx, sz + 1, 0)) unvisited++;
		if (cellEquals(sx, sz - 1, 0)) unvisited++;
		if (unvisited == 0) {
			return;
		}

		int roll = this.rand.nextInt(unvisited);
		int dx = 0;
		int dz = 0;

		if (cellEquals(sx + 1, sz, 0)) {
			if (roll == 0) {
				dx = sx + 1;
				dz = sz;
			}
			roll--;
		}
		if (cellEquals(sx - 1, sz, 0)) {
			if (roll == 0) {
				dx = sx - 1;
				dz = sz;
			}
			roll--;
		}
		if (cellEquals(sx, sz + 1, 0)) {
			if (roll == 0) {
				dx = sx;
				dz = sz + 1;
			}
			roll--;
		}
		if (cellEquals(sx, sz - 1, 0) && roll == 0) {
			dx = sx;
			dz = sz - 1;
		}

		putWall(sx, sz, dx, dz, 2);
		rbGen(dx, dz);
		rbGen(sx, sz);
		rbGen(sx, sz);
	}

	public void add4Exits() {
		int hx = this.rawWidth / 2 + 1;
		int hz = this.rawDepth / 2 + 1;
		putRaw(hx, 0, ROOM);
		putRaw(hx, this.rawDepth - 1, ROOM);
		putRaw(0, hz, ROOM);
		putRaw(this.rawWidth - 1, hz, ROOM);
	}

	public void carveRoom1(int cx, int cz) {
		int rx = cx * 2 + 1;
		int rz = cz * 2 + 1;

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				putRaw(rx + i, rz + j, ROOM);
			}
		}

		putCell(rx, rz + 1, 0);
		putCell(rx, rz - 1, 0);
		putCell(rx + 1, rz, 0);
		putCell(rx - 1, rz, 0);

		if (getRaw(rx, rz + 4) != OUT_OF_BOUNDS) putRaw(rx, rz + 3, ROOM);
		if (getRaw(rx, rz - 4) != OUT_OF_BOUNDS) putRaw(rx, rz - 3, ROOM);
		if (getRaw(rx + 4, rz) != OUT_OF_BOUNDS) putRaw(rx + 3, rz, ROOM);
		if (getRaw(rx - 4, rz) != OUT_OF_BOUNDS) putRaw(rx - 3, rz, ROOM);
	}

	public void copyToWorld(World world, int dx, int dy, int dz) {
		this.worldX = dx;
		this.worldY = dy;
		this.worldZ = dz;

		for (int x = 0; x < this.rawWidth; x++) {
			for (int z = 0; z < this.rawDepth; z++) {
				if (getRaw(x, z) != 0) {
					continue;
				}
				int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
				int mdz = dz + z / 2 * (this.evenBias + this.oddBias);

				if (isEven(x) && isEven(z)) {

					if (this.type == 4 && shouldTree(x, z)) {
						new WorldFeatureTFCanopyTree().place(world, this.rand, mdx, dy, mdz);
					} else {
						column(world, mdx, dy, mdz);
					}
				} else if (isEven(x)) {
					for (int i = 1; i <= this.oddBias; i++) {
						column(world, mdx, dy, mdz + i);
					}
				} else if (isEven(z)) {
					for (int i = 1; i <= this.oddBias; i++) {
						column(world, mdx + i, dy, mdz);
					}
				}
			}
		}

		placeTorches(world);
	}

	private void column(World world, int x, int y, int z) {
		for (int i = 0; i < this.tall; i++) {
			world.setBlockWithNotify(x, y + i, z, this.wallBlockId);
		}
		for (int i = 1; i <= this.roots; i++) {
			world.setBlockWithNotify(x, y - i, z, this.rootBlockId);
		}
	}

	public void carveToWorld(World world, int dx, int dy, int dz) {
		this.worldX = dx;
		this.worldY = dy;
		this.worldZ = dz;

		for (int x = 0; x < this.rawWidth; x++) {
			for (int z = 0; z < this.rawDepth; z++) {
				if (getRaw(x, z) == 0) {
					continue;
				}
				int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
				int mdz = dz + z / 2 * (this.evenBias + this.oddBias);

				if (isEven(x) && isEven(z)) {
					carveColumn(world, mdx, dy, mdz);
				} else if (isEven(x)) {
					for (int i = 1; i <= this.oddBias; i++) {
						carveColumn(world, mdx, dy, mdz + i);
					}
				} else if (isEven(z)) {
					for (int i = 1; i <= this.oddBias; i++) {
						carveColumn(world, mdx + i, dy, mdz);
					}
				} else {
					for (int mx = 1; mx <= this.oddBias; mx++) {
						for (int mz = 1; mz <= this.oddBias; mz++) {
							carveColumn(world, mdx + mx, dy, mdz + mz);
						}
					}
				}
			}
		}

		placeTorches(world);
	}

	private void carveColumn(World world, int x, int y, int z) {
		for (int i = 0; i < this.tall; i++) {
			world.setBlockWithNotify(x, y + i, z, 0);
		}
	}

	public void placeTorches(World world) {
		for (int x = 0; x < this.rawWidth; x++) {
			for (int z = 0; z < this.rawDepth; z++) {
				if (getRaw(x, z) != 0 || !isEven(x) || !isEven(z) || !shouldTorch(x, z)) {
					continue;
				}
				int mdx = this.worldX + x / 2 * (this.evenBias + this.oddBias);
				int mdy = this.worldY + 1;
				int mdz = this.worldZ + z / 2 * (this.evenBias + this.oddBias);
				if (world.getBlockId(mdx, mdy, mdz) == this.wallBlockId) {
					world.setBlockAndMetadataWithNotify(mdx, mdy, mdz, this.torchBlockId, this.torchBlockMeta);
				}
			}
		}
	}

	public boolean shouldTorch(int rx, int rz) {
		if (getRaw(rx + 1, rz) == OUT_OF_BOUNDS || getRaw(rx - 1, rz) == OUT_OF_BOUNDS
			|| getRaw(rx, rz + 1) == OUT_OF_BOUNDS || getRaw(rx, rz - 1) == OUT_OF_BOUNDS) {
			return false;
		}
		boolean openOnX = getRaw(rx + 1, rz) != 0 || getRaw(rx - 1, rz) != 0;
		boolean openOnZ = getRaw(rx, rz + 1) != 0 || getRaw(rx, rz - 1) != 0;
		return openOnX && openOnZ && this.rand.nextInt(4) == 0;
	}

	public boolean shouldTree(int rx, int rz) {
		boolean onXEdge = rx == 0 || rx == this.rawWidth - 1;
		boolean onZEdge = rz == 0 || rz == this.rawDepth - 1;

		if (onXEdge && (getRaw(rx, rz + 1) != 0 || getRaw(rx, rz - 1) != 0)) {
			return true;
		}
		if (onZEdge && (getRaw(rx + 1, rz) != 0 || getRaw(rx - 1, rz) != 0)) {
			return true;
		}
		return this.rand.nextInt(50) == 0;
	}

	public int getWorldX(int x) {
		return this.worldX + x * (this.evenBias + this.oddBias) + 1;
	}

	public int getWorldZ(int z) {
		return this.worldZ + z * (this.evenBias + this.oddBias) + 1;
	}
}
