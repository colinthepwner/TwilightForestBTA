package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeMound extends StructureComponentTF {

	public static final int DIAMETER = 35;

	private static final int HEIGHT = 11;

	private static final int ARM_FLOOR = 6;

	private int averageGroundLevel = -1;
	private ComponentTFMazeUpperEntrance mazeAbove;

	public ComponentTFMazeMound(int componentType, Random rand, int x, int y, int z) {
		super(componentType);

		this.coordBaseMode = rand.nextInt(4);

		this.boundingBox = new BoundingBox(x, y, z, x + DIAMETER, y + 8, z + DIAMETER);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		super.buildComponent(parent, pieces, rand);
		this.mazeAbove = new ComponentTFMazeUpperEntrance(3, rand,
			this.boundingBox.minX + 10, this.boundingBox.minY + 1, this.boundingBox.minZ + 10);
		pieces.add(this.mazeAbove);
		this.mazeAbove.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = getAverageGroundLevel(world, clip);
			if (this.averageGroundLevel < 0) {
				return true;
			}
			int offset = this.averageGroundLevel - this.boundingBox.maxY + 8 - 1;
			this.boundingBox.offset(0, offset, 0);

			if (this.mazeAbove != null) {
				this.mazeAbove.boundingBox.offset(0, offset, 0);
			}
		}

		int grass = Blocks.GRASS.id();
		int dirt = Blocks.DIRT.id();

		for (int x = 0; x < DIAMETER; x++) {
			for (int z = 0; z < DIAMETER; z++) {

				int cx = x - 17;
				int cz = z - 17;
				int dist = (int) Math.sqrt((double) cx * cx + (double) cz * cz);
				int hheight = (int) (Math.cos((double) dist / DIAMETER * Math.PI) * HEIGHT);

				boolean armX = cx <= 2 && cx >= -1;
				boolean armZ = cz <= 2 && cz >= -1;

				if (armX && armZ || (armX || armZ) && hheight <= ARM_FLOOR) {
					continue;
				}

				placeBlock(world, grass, 0, x, hheight, z, clip);

				if (!(armX || armZ)) {

					fillBlocksDownwards(world, dirt, 0, x, hheight - 1, z, clip);
					continue;
				}

				fillWithBlocks(world, clip, x, ARM_FLOOR, z, x, hheight - 1, z, dirt, 0, false);
			}
		}

		return true;
	}

	protected void fillBlocksDownwards(World world, int blockId, int meta,
	                                   int x, int y, int z, BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);

		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		while (wy > 1
			&& (world.isAirBlock(wx, wy, wz)
			|| world.getBlockMaterial(new TilePos(wx, wy, wz)).isLiquid())) {
			world.setBlockAndMetadataRaw(wx, wy, wz, blockId, meta);
			wy--;
		}
	}

	protected int getAverageGroundLevel(World world, BoundingBox clip) {
		int total = 0;
		int count = 0;
		for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
			for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
				if (!clip.contains(x, 64, z)) {
					continue;
				}
				total += Math.max(world.getHeightValue(x, z), StructureStartTF.BASE_Y);
				count++;
			}
		}
		if (count == 0) {
			return -1;
		}
		return total / count;
	}
}
