package com.twilightforest.world.structure;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.MobTFMinoshroom;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFMazeRoomBoss extends ComponentTFMazeRoom {

	private static final int MYCELIUM_STANDIN = Blocks.MUD.id();

	private static final int MUSHROOM_BROWN = Blocks.MUSHROOM_BROWN.id();
	private static final int MUSHROOM_RED = Blocks.MUSHROOM_RED.id();

	private boolean taurPlaced = false;

	public ComponentTFMazeRoomBoss(int componentType, Random rand, int x, int y, int z) {
		super(componentType, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int giantBrown = TFBlocks.MUSHROOM_GIANT_BROWN.id();
		int giantRed = TFBlocks.MUSHROOM_GIANT_RED.id();
		int skin = BlockLogicTFGiantMushroom.ALL_SKIN;

		if (getBlockIdAt(world, 7, 1, 0, clip) == 0) {
			fillWithBlocks(world, clip, 6, 1, 0, 9, 4, 0, FENCE, 0, false);
		}
		if (getBlockIdAt(world, 7, 1, 15, clip) == 0) {
			fillWithBlocks(world, clip, 6, 1, 15, 9, 4, 15, FENCE, 0, false);
		}
		if (getBlockIdAt(world, 0, 1, 7, clip) == 0) {
			fillWithBlocks(world, clip, 0, 1, 6, 0, 4, 9, FENCE, 0, false);
		}
		if (getBlockIdAt(world, 15, 1, 7, clip) == 0) {
			fillWithBlocks(world, clip, 15, 1, 6, 15, 4, 9, FENCE, 0, false);
		}

		for (int x = 1; x < 14; x++) {
			for (int z = 1; z < 14; z++) {
				int dist = (int) Math.round(
					7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));

				boolean mycelium = rand.nextInt(dist + 1) > 0;
				boolean mushroom = rand.nextInt(dist) > 0;
				boolean mushRed = rand.nextBoolean();

				if (mycelium) {
					placeBlock(world, MYCELIUM_STANDIN, 0, x, 0, z, clip);
				}
				if (!mushroom) {
					continue;
				}
				placeBlock(world, mushRed ? MUSHROOM_RED : MUSHROOM_BROWN, 0, x, 1, z, clip);
			}
		}

		fillWithBlocks(world, clip, 1, 1, 1, 3, 1, 3, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 2, 1, 1, 3, 4, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 2, 2, 1, 4, 3, 1, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 4, 1, 3, 4, 3, giantRed, skin, AIR, 0, false);
		placeTreasure(world, rand, 3, 2, 3, TFTreasure.LABYRINTH_ROOM, clip);

		fillWithBlocks(world, clip, 12, 1, 12, 14, 1, 14, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 14, 2, 11, 14, 3, 14, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 11, 2, 14, 14, 3, 14, giantRed, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 12, 4, 12, 14, 4, 14, giantRed, skin, AIR, 0, false);
		placeTreasure(world, rand, 12, 2, 12, TFTreasure.LABYRINTH_ROOM, clip);

		fillWithBlocks(world, clip, 1, 1, 12, 3, 1, 14, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 2, 11, 1, 3, 14, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 2, 2, 14, 4, 3, 14, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 1, 4, 12, 3, 4, 14, giantBrown, skin, AIR, 0, false);
		placeTreasure(world, rand, 3, 2, 12, TFTreasure.LABYRINTH_ROOM, clip);

		fillWithBlocks(world, clip, 12, 1, 1, 14, 1, 3, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 11, 2, 1, 14, 3, 1, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 14, 2, 2, 14, 3, 4, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 12, 4, 1, 14, 4, 3, giantBrown, skin, AIR, 0, false);
		placeTreasure(world, rand, 12, 2, 3, TFTreasure.LABYRINTH_ROOM, clip);

		fillWithBlocks(world, clip, 5, 4, 5, 7, 5, 7, giantBrown, skin, AIR, 0, false);
		fillWithBlocks(world, clip, 8, 4, 8, 10, 5, 10, giantRed, skin, AIR, 0, false);

		placeMinoshroom(world, clip);

		return true;
	}

	private void placeMinoshroom(World world, BoundingBox clip) {
		int bx = getXWithOffset(7, 7);
		int by = getYWithOffset(1);
		int bz = getZWithOffset(7, 7);

		if (this.taurPlaced || !clip.contains(bx, by, bz)) {
			return;
		}
		this.taurPlaced = true;

		MobTFMinoshroom taur = new MobTFMinoshroom(world);
		taur.moveTo(bx + 0.5, by, bz + 0.5, 0.0F, 0.0F);
		world.entityJoinedWorld(taur);
	}
}
