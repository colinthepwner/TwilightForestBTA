package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import com.twilightforest.world.treasure.TFTreasure;
import com.twilightforest.world.treasure.TFTreasureTable;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.List;
import java.util.Random;

public abstract class StructureComponentTF {

	public static class BoundingBox {
		public int minX;
		public int minY;
		public int minZ;
		public int maxX;
		public int maxY;
		public int maxZ;

		public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;
		}

		public static BoundingBox forChunk(int chunkX, int chunkZ, int maxY) {
			return new BoundingBox(chunkX * 16, 0, chunkZ * 16,
				chunkX * 16 + 15, maxY, chunkZ * 16 + 15);
		}

		public boolean contains(int x, int y, int z) {
			return x >= this.minX && x <= this.maxX
				&& z >= this.minZ && z <= this.maxZ
				&& y >= this.minY && y <= this.maxY;
		}

		public boolean intersects(BoundingBox other) {
			return this.maxX >= other.minX && this.minX <= other.maxX
				&& this.maxZ >= other.minZ && this.minZ <= other.maxZ
				&& this.maxY >= other.minY && this.minY <= other.maxY;
		}

		public void offset(int dx, int dy, int dz) {
			this.minX += dx;
			this.minY += dy;
			this.minZ += dz;
			this.maxX += dx;
			this.maxY += dy;
			this.maxZ += dz;
		}

		public void expandTo(BoundingBox other) {
			this.minX = Math.min(this.minX, other.minX);
			this.minY = Math.min(this.minY, other.minY);
			this.minZ = Math.min(this.minZ, other.minZ);
			this.maxX = Math.max(this.maxX, other.maxX);
			this.maxY = Math.max(this.maxY, other.maxY);
			this.maxZ = Math.max(this.maxZ, other.maxZ);
		}

		public int getYSize() {
			return this.maxY - this.minY + 1;
		}

		public boolean intersectsColumn(int x1, int z1, int x2, int z2) {
			return this.maxX >= Math.min(x1, x2) && this.minX <= Math.max(x1, x2)
				&& this.maxZ >= Math.min(z1, z2) && this.minZ <= Math.max(z1, z2);
		}

		public BoundingBox copy() {
			return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
		}
	}

	public static StructureComponentTF findIntersecting(List<StructureComponentTF> pieces,
	                                                    BoundingBox box) {
		for (StructureComponentTF piece : pieces) {
			if (piece.boundingBox != null && piece.boundingBox.intersects(box)) {
				return piece;
			}
		}
		return null;
	}

	public BoundingBox boundingBox;

	public int coordBaseMode;

	protected final int componentType;

	public StructureTFDecorator deco = null;

	public int spawnListIndex = 0;

	protected StructureComponentTF(int componentType) {
		this.componentType = componentType;
	}

	private static final StructureTFStrongholdStones STRONGHOLD_STONES =
		new StructureTFStrongholdStones();

	public static StructureTFStrongholdStones getStrongholdStones() {
		return STRONGHOLD_STONES;
	}

	private static final StructureTFTowerWoods TOWER_WOODS = new StructureTFTowerWoods();

	public static StructureTFTowerWoods getTowerWoods() {
		return TOWER_WOODS;
	}

	public static BoundingBox componentBox(int x, int y, int z,
	                                       int minX, int minY, int minZ,
	                                       int maxX, int maxY, int maxZ, int dir) {
		switch (dir) {
			case 1:
				return new BoundingBox(x - maxZ + minZ, y + minY, z + minX,
					x + minZ, y + maxY + minY, z + maxX + minX);
			case 2:
				return new BoundingBox(x - maxX - minX, y + minY, z - maxZ - minZ,
					x - minX, y + maxY + minY, z - minZ);
			case 3:
				return new BoundingBox(x + minZ, y + minY, z - maxX,
					x + maxZ + minZ, y + maxY + minY, z + minX);
			case 0:
			default:
				return new BoundingBox(x + minX, y + minY, z + minZ,
					x + maxX + minX, y + maxY + minY, z + maxZ + minZ);
		}
	}

	protected int getXWithOffset(int x, int z) {
		switch (this.coordBaseMode) {
			case 1: return this.boundingBox.maxX - z;
			case 2: return this.boundingBox.maxX - x;
			case 3: return this.boundingBox.minX + z;
			case 0:
			default: return this.boundingBox.minX + x;
		}
	}

	protected int getYWithOffset(int y) {
		return this.boundingBox.minY + y;
	}

	protected int getZWithOffset(int x, int z) {
		switch (this.coordBaseMode) {
			case 1: return this.boundingBox.minZ + x;
			case 2: return this.boundingBox.maxZ - z;
			case 3: return this.boundingBox.maxZ - x;
			case 0:
			default: return this.boundingBox.minZ + z;
		}
	}

	public int getCoordBaseMode() {
		return this.coordBaseMode;
	}

	public void setCoordBaseMode(int coordBaseMode) {
		this.coordBaseMode = coordBaseMode;
	}

	protected int getXWithOffsetAsIfRotated(int x, int z, int rotation) {
		if (this.coordBaseMode < 0) {
			return x;
		}
		switch ((this.coordBaseMode + rotation) % 4) {
			case 1: return this.boundingBox.maxX - z;
			case 2: return this.boundingBox.maxX - x;
			case 3: return this.boundingBox.minX + z;
			case 0:
			default: return this.boundingBox.minX + x;
		}
	}

	protected int getZWithOffsetAsIfRotated(int x, int z, int rotation) {
		if (this.coordBaseMode < 0) {
			return z;
		}
		switch ((this.coordBaseMode + rotation) % 4) {
			case 1: return this.boundingBox.minZ + x;
			case 2: return this.boundingBox.maxZ - z;
			case 3: return this.boundingBox.maxZ - x;
			case 0:
			default: return this.boundingBox.minZ + z;
		}
	}

	public int[] getOffsetAsIfRotated(int[] src, int rotation) {
		int temp = this.coordBaseMode;
		this.coordBaseMode = rotation;
		int[] dest = {
			getXWithOffset(src[0], src[2]),
			getYWithOffset(src[1]),
			getZWithOffset(src[0], src[2]),
		};
		this.coordBaseMode = temp;
		return dest;
	}

	protected int[] offsetTowerCoords(int x, int y, int z, int towerSize, int direction) {
		int dx = getXWithOffset(x, z);
		int dy = getYWithOffset(y);
		int dz = getZWithOffset(x, z);

		switch (direction) {
			case 0: return new int[]{dx + 1, dy - 1, dz - towerSize / 2};
			case 1: return new int[]{dx + towerSize / 2, dy - 1, dz + 1};
			case 2: return new int[]{dx - 1, dy - 1, dz + towerSize / 2};
			case 3: return new int[]{dx - towerSize / 2, dy - 1, dz - 1};
			default: return new int[]{x, y, z};
		}
	}

	public static final int[] STAIR_META = {1, 0, 3, 2};

	protected int getStairMeta(int dir) {
		switch ((this.coordBaseMode + dir) % 4) {
			case 0: return STAIR_META[0];
			case 1: return STAIR_META[2];
			case 2: return STAIR_META[1];
			case 3: return STAIR_META[3];
			default: return -1;
		}
	}

	protected int getLadderMeta(int ladderDir) {
		switch ((this.coordBaseMode + ladderDir) % 4) {
			case 0: return 4;
			case 1: return 2;
			case 2: return 5;
			case 3: return 3;
			default: return -1;
		}
	}

	protected int getLadderMeta(int ladderDir, int rotation) {
		return getLadderMeta(ladderDir + rotation);
	}

	protected void placeSpawner(World world, Random rand, int x, int y, int z,
	                            String mobId, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);

		if (!clip.contains(wx, wy, wz) || world.getBlockId(wx, wy, wz) == Blocks.MOBSPAWNER.id()) {
			return;
		}

		world.setBlockWithNotify(wx, wy, wz, Blocks.MOBSPAWNER.id());
		if (world.getTileEntity(wx, wy, wz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(mobId);
		}
	}

	protected void placeTreasure(World world, Random rand, int x, int y, int z,
	                             TFTreasureTable table, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)
			|| world.getBlockId(wx, wy, wz) == Blocks.CHEST_PLANKS_OAK.id()) {
			return;
		}
		TFTreasure.place(world, rand, wx, wy, wz, table);
	}

	protected void placeTreasureRotated(World world, Random rand, int x, int y, int z, int rotation,
	                                    TFTreasureTable table, BoundingBox clip) {
		int wx = getXWithOffsetAsIfRotated(x, z, rotation);
		int wy = getYWithOffset(y);
		int wz = getZWithOffsetAsIfRotated(x, z, rotation);
		if (!clip.contains(wx, wy, wz)
			|| world.getBlockId(wx, wy, wz) == Blocks.CHEST_PLANKS_OAK.id()) {
			return;
		}
		TFTreasure.place(world, rand, wx, wy, wz, table);
	}

	public int componentType() {
		return this.componentType;
	}

	protected void placeBlock(World world, int blockId, int meta, int x, int y, int z,
	                          BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		placeBlockAbsolute(world, blockId, meta, wx, wy, wz);
	}

	protected static void placeBlockAbsolute(World world, int blockId, int meta,
	                                         int wx, int wy, int wz) {
		Block<?> block = blockId > 0 && blockId < Blocks.blocksList.length
			? Blocks.blocksList[blockId]
			: null;

		if (block != null && block.getLogic().getMaterial().isLiquid()) {

			world.setBlockAndMetadataWithNotify(wx, wy, wz, blockId, meta);
			return;
		}

		world.setBlockAndMetadataRaw(wx, wy, wz, blockId, meta);
	}

	protected int getBlockIdAt(World world, int x, int y, int z, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)) {
			return 0;
		}
		return world.getBlockId(wx, wy, wz);
	}

	protected void fillWithRandomizedBlocks(World world, BoundingBox clip,
	                                        int minX, int minY, int minZ,
	                                        int maxX, int maxY, int maxZ,
	                                        boolean alwaysReplace, Random rand,
	                                        BlockSelector selector) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (alwaysReplace && getBlockIdAt(world, x, y, z, clip) == 0) {
						continue;
					}
					boolean shell = y == minY || y == maxY
						|| x == minX || x == maxX
						|| z == minZ || z == maxZ;
					selector.select(rand, x, y, z, shell);
					placeBlock(world, selector.blockId, selector.meta, x, y, z, clip);
				}
			}
		}
	}

	protected void fillWithBlocks(World world, BoundingBox clip,
	                              int minX, int minY, int minZ,
	                              int maxX, int maxY, int maxZ,
	                              int shellId, int shellMeta,
	                              int interiorId, int interiorMeta,
	                              boolean alwaysReplace) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (alwaysReplace && getBlockIdAt(world, x, y, z, clip) == 0) {
						continue;
					}
					boolean shell = y == minY || y == maxY
						|| x == minX || x == maxX
						|| z == minZ || z == maxZ;
					if (shell) {
						placeBlock(world, shellId, shellMeta, x, y, z, clip);
					} else {
						placeBlock(world, interiorId, interiorMeta, x, y, z, clip);
					}
				}
			}
		}
	}

	protected void fillWithBlocks(World world, BoundingBox clip,
	                              int minX, int minY, int minZ,
	                              int maxX, int maxY, int maxZ,
	                              int shellId, int shellMeta, boolean alwaysReplace) {
		fillWithBlocks(world, clip, minX, minY, minZ, maxX, maxY, maxZ,
			shellId, shellMeta, 0, 0, alwaysReplace);
	}

	protected void placeBlockRotated(World world, int blockId, int meta,
	                                 int x, int y, int z, int rotation, BoundingBox clip) {
		int wx = getXWithOffsetAsIfRotated(x, z, rotation);
		int wy = getYWithOffset(y);
		int wz = getZWithOffsetAsIfRotated(x, z, rotation);
		if (!clip.contains(wx, wy, wz)) {
			return;
		}

		placeBlockAbsolute(world, blockId, meta, wx, wy, wz);
	}

	protected int getBlockIdRotated(World world, int x, int y, int z, int rotation,
	                                BoundingBox clip) {
		int wx = getXWithOffsetAsIfRotated(x, z, rotation);
		int wy = getYWithOffset(y);
		int wz = getZWithOffsetAsIfRotated(x, z, rotation);
		if (!clip.contains(wx, wy, wz)) {
			return 0;
		}
		return world.getBlockId(wx, wy, wz);
	}

	protected void fillBlocksRotated(World world, BoundingBox clip,
	                                 int minX, int minY, int minZ,
	                                 int maxX, int maxY, int maxZ,
	                                 int blockId, int meta, int rotation) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					placeBlockRotated(world, blockId, meta, x, y, z, rotation, clip);
				}
			}
		}
	}

	protected void fillAirRotated(World world, BoundingBox clip,
	                              int minX, int minY, int minZ,
	                              int maxX, int maxY, int maxZ, int rotation) {
		fillBlocksRotated(world, clip, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, rotation);
	}

	protected void placeSpawnerRotated(World world, int x, int y, int z, int rotation,
	                                   String mobId, BoundingBox clip) {
		int wx = getXWithOffsetAsIfRotated(x, z, rotation);
		int wy = getYWithOffset(y);
		int wz = getZWithOffsetAsIfRotated(x, z, rotation);

		if (!clip.contains(wx, wy, wz) || world.getBlockId(wx, wy, wz) == Blocks.MOBSPAWNER.id()) {
			return;
		}

		world.setBlockWithNotify(wx, wy, wz, Blocks.MOBSPAWNER.id());
		if (world.getTileEntity(wx, wy, wz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(mobId);
		}
	}

	public void nullifySkyLight(World world, int sx, int sy, int sz, int dx, int dy, int dz) {
		for (int x = sx; x <= dx; x++) {
			for (int z = sz; z <= dz; z++) {
				for (int y = sy; y <= dy; y++) {
					world.setLightValue(LightLayer.Sky, new TilePos(x, y, z), 0);
				}
			}
		}
	}

	public void nullifySkyLightForBoundingBox(World world) {
		nullifySkyLight(world,
			this.boundingBox.minX - 1, this.boundingBox.minY - 1, this.boundingBox.minZ - 1,
			this.boundingBox.maxX + 1, this.boundingBox.maxY + 1, this.boundingBox.maxZ + 1);
	}

	public void nullifySkyLightAtCurrentPosition(World world, int sx, int sy, int sz,
	                                             int dx, int dy, int dz) {
		nullifySkyLight(world,
			getXWithOffset(sx, sz), getYWithOffset(sy), getZWithOffset(sx, sz),
			getXWithOffset(dx, dz), getYWithOffset(dy), getZWithOffset(dx, dz));
	}

	public abstract static class BlockSelector {
		public int blockId;
		public int meta;

		public abstract void select(Random rand, int x, int y, int z, boolean shell);
	}

	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {

	}

	public abstract boolean addComponentParts(World world, Random rand, BoundingBox clip);

	public abstract int featureType();

	protected static boolean isHill(int type) {
		return type == TFFeature.SMALL_HILL || type == TFFeature.MEDIUM_HILL
			|| type == TFFeature.LARGE_HILL;
	}
}
