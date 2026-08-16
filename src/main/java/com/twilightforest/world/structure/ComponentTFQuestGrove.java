package com.twilightforest.world.structure;

import com.twilightforest.entity.MobTFQuestRam;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityDispenser;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFQuestGrove extends StructureComponentTF {

	public static final int RADIUS = 13;

	private static final int MOSSY = Blocks.BRICK_STONE_POLISHED_MOSSY.id();

	private static final int CARVED = Blocks.STONE_CARVED.id();

	private static final int DISPENSER = Blocks.DISPENSER_COBBLE_STONE.id();
	private static final int BUTTON = Blocks.BUTTON_STONE.id();
	private static final int WOOL = Blocks.WOOL.id();

	private boolean beastPlaced = false;
	private boolean dispenserPlaced = false;

	public ComponentTFQuestGrove(int componentType, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = 0;

		this.boundingBox = componentBox(x, y, z, -RADIUS, 0, -RADIUS, 26, 10, 26, 0);
	}

	@Override
	public int featureType() {
		return TFFeature.QUEST_GROVE;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		for (int i = 0; i < 4; i++) {
			makeWallSide(world, rand, i, clip);
		}

		for (int x = 10; x < 17; x++) {
			for (int z = 10; z < 17; z++) {
				if (x == 10 || x == 16 || z == 10 || z == 16) {
					if (rand.nextInt(2) <= 0) {
						continue;
					}
					placeBlock(world, MOSSY, 0, x, -1, z, clip);
				} else if (x == 11 || x == 15 || z == 11 || z == 15) {
					if (rand.nextInt(3) <= 0) {
						continue;
					}
					placeBlock(world, MOSSY, 0, x, -1, z, clip);
				} else {
					placeBlock(world, MOSSY, 0, x, -1, z, clip);
				}
			}
		}

		placeBlock(world, BUTTON, 4, 13, 5, 19, clip);
		for (int x = 12; x <= 14; x++) {
			placeBlock(world, MOSSY, 0, x, 7, 20, clip);
			placeBlock(world, MOSSY, 0, x, 7, 21, clip);
		}

		placeWoolDispenser(world, rand, clip);
		placeQuestRam(world, clip);

		return true;
	}

	private void placeWoolDispenser(World world, Random rand, BoundingBox clip) {
		int wx = getXWithOffset(13, 20);
		int wy = getYWithOffset(6);
		int wz = getZWithOffset(13, 20);

		if (this.dispenserPlaced || !clip.contains(wx, wy, wz)) {
			return;
		}
		this.dispenserPlaced = true;

		world.setBlockAndMetadataWithNotify(wx, wy, wz, DISPENSER, 2);
		if (world.getTileEntity(wx, wy, wz) instanceof TileEntityDispenser dispenser) {
			for (int i = 0; i < 4; i++) {
				dispenser.setItem(i, new ItemStack(WOOL, 1, rand.nextInt(16)));
			}
		}
	}

	private void placeQuestRam(World world, BoundingBox clip) {
		int wx = getXWithOffset(13, 13);
		int wy = getYWithOffset(0);
		int wz = getZWithOffset(13, 13);

		if (this.beastPlaced || !clip.contains(wx, wy, wz)) {
			return;
		}
		this.beastPlaced = true;

		MobTFQuestRam ram = new MobTFQuestRam(world);
		ram.moveTo(wx + 0.5, wy, wz + 0.5, 0.0F, 0.0F);
		world.entityJoinedWorld(ram);
	}

	private void makeWallSide(World world, Random rand, int direction, BoundingBox clip) {
		int temp = getCoordBaseMode();
		setCoordBaseMode(direction);

		placeOuterArch(world, 3, -1, clip);
		placeOuterArch(world, 11, -1, clip);
		placeOuterArch(world, 19, -1, clip);

		for (int y = 0; y <= 3; y++) {
			placeBlock(world, CARVED, 0, 0, y, 0, clip);
		}
		for (int x : new int[]{1, 2, 8, 9, 10, 16, 17, 18, 24, 25}) {
			placeBlock(world, CARVED, 0, x, 3, 0, clip);
		}

		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				for (int z = 0; z < 2; z++) {
					boolean edge = x == 0 || x == 1 || x == 7 || x == 8
						|| y == 0 || y == 1 || y == 7 || y == 8;
					if (!edge) {
						continue;
					}
					placeBlock(world, MOSSY, 0, x + 9, y - 2, z + 5, clip);
				}
			}
		}

		for (int y = 0; y <= 4; y++) {
			placeBlock(world, CARVED, 0, 6, y, 6, clip);
		}
		for (int x : new int[]{7, 8, 18, 19}) {
			placeBlock(world, CARVED, 0, x, 4, 6, clip);
		}

		setCoordBaseMode(temp);
	}

	private void placeOuterArch(World world, int ox, int oy, BoundingBox clip) {
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 6; y++) {
				if (x != 0 && x != 4 && y != 0 && y != 5) {
					continue;
				}
				placeBlock(world, MOSSY, 0, x + ox, y + oy, 0, clip);
			}
		}
	}
}
