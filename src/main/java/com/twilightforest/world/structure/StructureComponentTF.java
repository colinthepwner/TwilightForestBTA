package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.world.World;

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

	protected StructureComponentTF(int componentType) {
		this.componentType = componentType;
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
