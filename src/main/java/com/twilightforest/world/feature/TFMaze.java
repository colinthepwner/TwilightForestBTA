package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.structure.StructureComponentTF;
import net.minecraft.core.block.BlockLogicTorch;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class TFMaze {

	static final int OUT_OF_BOUNDS = Integer.MIN_VALUE;

	static final int ROOM = 5;

	static final int DOOR = 6;

	public final int width;
	public final int depth;

	private final int rawWidth;
	private final int rawDepth;
	private final int[] storage;

	public int oddBias = 3;

	public int evenBias = 1;

	public int tall = 3;

	public int head = 0;

	public int roots = 0;

	public int type;

	public int wallBlockId = TFBlocks.MAZESTONE_MOSSY.id();

	public int wallBlockMeta = 0;

	public int wallVar0Id;
	public int wallVar0Meta;

	public float wallVarRarity = 0.0f;

	public int headBlockId;
	public int headBlockMeta;

	public int rootBlockId = TFBlocks.MAZESTONE.id();
	public int rootBlockMeta = 0;

	public int pillarBlockId = -1;
	public int pillarBlockMeta = 0;

	public int doorBlockId;
	public int doorBlockMeta;

	public float doorRarity = 0.0f;

	public int torchBlockId = Blocks.TORCH_COAL.id();

	public int torchBlockMeta = BlockLogicTorch.SIDE_BOTTOM;

	public float torchRarity = 0.75f;

	public int worldX;
	public int worldY;
	public int worldZ;

	public final Random rand = new Random();

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

	public void putRaw(int rawX, int rawZ, int value) {
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

		if (this.rand.nextFloat() <= this.doorRarity) {
			putWall(sx, sz, dx, dz, DOOR);
		} else {
			putWall(sx, sz, dx, dz, 2);
		}

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

	public void carveRoom0(int cx, int cz) {
		putCell(cx, cz, ROOM);
		putCell(cx + 1, cz, ROOM);
		putWall(cx, cz, cx + 1, cz, ROOM);
		putCell(cx - 1, cz, ROOM);
		putWall(cx, cz, cx - 1, cz, ROOM);
		putCell(cx, cz + 1, ROOM);
		putWall(cx, cz, cx, cz + 1, ROOM);
		putCell(cx, cz - 1, ROOM);
		putWall(cx, cz, cx, cz - 1, ROOM);
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
					for (int even = 0; even < this.evenBias; even++) {
						for (int odd = 1; odd <= this.oddBias; odd++) {
							column(world, mdx + even, dy, mdz + odd);
						}
					}
				} else if (isEven(z)) {
					for (int even = 0; even < this.evenBias; even++) {
						for (int odd = 1; odd <= this.oddBias; odd++) {
							column(world, mdx + odd, dy, mdz + even);
						}
					}
				}
			}
		}

		placeTorches(world);
	}

	private void column(World world, int x, int y, int z) {
		for (int i = 0; i < this.head; i++) {
			world.setBlockAndMetadataWithNotify(x, y + this.tall + i, z, this.headBlockId, this.headBlockMeta);
		}
		for (int i = 0; i < this.tall; i++) {
			world.setBlockAndMetadataWithNotify(x, y + i, z, this.wallBlockId, this.wallBlockMeta);
		}
		for (int i = 1; i <= this.roots; i++) {
			world.setBlockAndMetadataWithNotify(x, y - i, z, this.rootBlockId, this.rootBlockMeta);
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

	public void copyToStructure(World world, int dx, int dy, int dz,
	                            StructureComponentTF component, StructureComponentTF.BoundingBox clip) {
		for (int x = 0; x < this.rawWidth; x++) {
			for (int z = 0; z < this.rawDepth; z++) {
				int raw = getRaw(x, z);
				if (raw != 0 && raw != DOOR) {
					continue;
				}

				int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
				int mdz = dz + z / 2 * (this.evenBias + this.oddBias);
				if (this.evenBias > 1) {
					mdx--;
					mdz--;
				}

				if (raw == 0) {
					if (isEven(x) && isEven(z)) {
						if (this.type == 4 && shouldTree(x, z)) {
							putCanopyTree(world, mdx, dy, mdz, component, clip);
						} else {

							for (int ex = 0; ex < this.evenBias; ex++) {
								for (int ez = 0; ez < this.evenBias; ez++) {
									postColumn(world, x, z, mdx + ex, dy, mdz + ez, component, clip);
								}
							}
						}
					} else if (isEven(x)) {
						for (int even = 0; even < this.evenBias; even++) {
							for (int odd = 1; odd <= this.oddBias; odd++) {
								wallColumn(world, mdx + even, dy, mdz + odd, component, clip);
							}
						}
					} else if (isEven(z)) {
						for (int even = 0; even < this.evenBias; even++) {
							for (int odd = 1; odd <= this.oddBias; odd++) {
								wallColumn(world, mdx + odd, dy, mdz + even, component, clip);
							}
						}
					}
				} else {

					if (isEven(x) && !isEven(z)) {
						for (int even = 0; even < this.evenBias; even++) {
							for (int odd = 1; odd <= this.oddBias; odd++) {
								doorColumn(world, mdx + even, dy, mdz + odd, component, clip);
							}
						}
					} else if (!isEven(x) && isEven(z)) {
						for (int even = 0; even < this.evenBias; even++) {
							for (int odd = 1; odd <= this.oddBias; odd++) {
								doorColumn(world, mdx + odd, dy, mdz + even, component, clip);
							}
						}
					}
				}
			}
		}

		for (int x = 0; x < this.rawWidth; x++) {
			for (int z = 0; z < this.rawDepth; z++) {
				if (getRaw(x, z) != 0 || !isEven(x) || !isEven(z) || !shouldTorch(x, z)) {
					continue;
				}
				int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
				int mdy = dy + 1;
				int mdz = dz + z / 2 * (this.evenBias + this.oddBias);

				if (getStructureBlockId(world, component, clip, mdx, mdy, mdz) == this.wallBlockId) {
					putStructureBlock(world, component, clip, this.torchBlockId, this.torchBlockMeta,
						mdx, mdy, mdz);
				}
			}
		}
	}

	private void postColumn(World world, int rawX, int rawZ, int x, int y, int z,
	                        StructureComponentTF component, StructureComponentTF.BoundingBox clip) {
		for (int i = 0; i < this.head; i++) {
			putStructureBlock(world, component, clip, this.headBlockId, this.headBlockMeta,
				x, y + this.tall + i, z);
		}
		boolean pillar = shouldPillar(rawX, rawZ);
		for (int i = 0; i < this.tall; i++) {
			if (pillar) {
				putStructureBlock(world, component, clip, this.pillarBlockId, this.pillarBlockMeta,
					x, y + i, z);
			} else {
				putWallBlock(world, component, clip, x, y + i, z);
			}
		}
		for (int i = 1; i <= this.roots; i++) {
			putStructureBlock(world, component, clip, this.rootBlockId, this.rootBlockMeta,
				x, y - i, z);
		}
	}

	private void wallColumn(World world, int x, int y, int z,
	                        StructureComponentTF component, StructureComponentTF.BoundingBox clip) {
		for (int i = 0; i < this.head; i++) {
			putStructureBlock(world, component, clip, this.headBlockId, this.headBlockMeta,
				x, y + this.tall + i, z);
		}
		for (int i = 0; i < this.tall; i++) {
			putWallBlock(world, component, clip, x, y + i, z);
		}
		for (int i = 1; i <= this.roots; i++) {
			putStructureBlock(world, component, clip, this.rootBlockId, this.rootBlockMeta,
				x, y - i, z);
		}
	}

	private void doorColumn(World world, int x, int y, int z,
	                        StructureComponentTF component, StructureComponentTF.BoundingBox clip) {
		for (int i = 0; i < this.head; i++) {
			putStructureBlock(world, component, clip, this.headBlockId, this.headBlockMeta,
				x, y + this.tall + i, z);
		}
		for (int i = 0; i < this.tall; i++) {
			putStructureBlock(world, component, clip, this.doorBlockId, this.doorBlockMeta,
				x, y + i, z);
		}
		for (int i = 1; i <= this.roots; i++) {
			putStructureBlock(world, component, clip, this.rootBlockId, this.rootBlockMeta,
				x, y - i, z);
		}
	}

	private void putWallBlock(World world, StructureComponentTF component,
	                          StructureComponentTF.BoundingBox clip, int x, int y, int z) {

		if (this.wallVarRarity > 0.0f && this.rand.nextFloat() < this.wallVarRarity) {
			putStructureBlock(world, component, clip, this.wallVar0Id, this.wallVar0Meta, x, y, z);
		} else {
			putStructureBlock(world, component, clip, this.wallBlockId, this.wallBlockMeta, x, y, z);
		}
	}

	private void putCanopyTree(World world, int x, int y, int z,
	                           StructureComponentTF component, StructureComponentTF.BoundingBox clip) {
		int[] w = toWorld(component, x, y, z);
		if (clip.contains(w[0], w[1], w[2])) {
			new WorldFeatureTFCanopyTree().place(world, this.rand, w[0], w[1], w[2]);
		}
	}

	private static int[] toWorld(StructureComponentTF component, int x, int y, int z) {
		return component.getOffsetAsIfRotated(new int[]{x, y, z}, component.getCoordBaseMode());
	}

	private static void putStructureBlock(World world, StructureComponentTF component,
	                                      StructureComponentTF.BoundingBox clip,
	                                      int blockId, int meta, int x, int y, int z) {
		int[] w = toWorld(component, x, y, z);
		if (!clip.contains(w[0], w[1], w[2])) {
			return;
		}

		world.setBlockAndMetadataRaw(w[0], w[1], w[2], blockId, meta);
	}

	private static int getStructureBlockId(World world, StructureComponentTF component,
	                                       StructureComponentTF.BoundingBox clip,
	                                       int x, int y, int z) {
		int[] w = toWorld(component, x, y, z);
		if (!clip.contains(w[0], w[1], w[2])) {
			return 0;
		}
		return world.getBlockId(w[0], w[1], w[2]);
	}

	public boolean shouldTorch(int rx, int rz) {
		if (getRaw(rx + 1, rz) == OUT_OF_BOUNDS || getRaw(rx - 1, rz) == OUT_OF_BOUNDS
			|| getRaw(rx, rz + 1) == OUT_OF_BOUNDS || getRaw(rx, rz - 1) == OUT_OF_BOUNDS) {
			return false;
		}
		boolean openOnX = getRaw(rx + 1, rz) != 0 || getRaw(rx - 1, rz) != 0;
		boolean openOnZ = getRaw(rx, rz + 1) != 0 || getRaw(rx, rz - 1) != 0;
		return openOnX && openOnZ && this.rand.nextFloat() <= this.torchRarity;
	}

	public boolean shouldPillar(int rx, int rz) {
		if (this.pillarBlockId == -1) {
			return false;
		}
		if (getRaw(rx + 1, rz) == OUT_OF_BOUNDS || getRaw(rx - 1, rz) == OUT_OF_BOUNDS
			|| getRaw(rx, rz + 1) == OUT_OF_BOUNDS || getRaw(rx, rz - 1) == OUT_OF_BOUNDS) {
			return false;
		}
		boolean openOnX = getRaw(rx + 1, rz) != 0 || getRaw(rx - 1, rz) != 0;
		boolean openOnZ = getRaw(rx, rz + 1) != 0 || getRaw(rx, rz - 1) != 0;
		return openOnX && openOnZ;
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
