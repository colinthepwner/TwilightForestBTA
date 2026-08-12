package com.twilightforest.world.treasure;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.Random;

public final class TFTreasure {
	private TFTreasure() {}

	public static final TFTreasureTable HILL_1 = hill("hill_1");

	public static final TFTreasureTable HILL_2 = hill("hill_2");

	public static final TFTreasureTable HILL_3 = hill("hill_3");

	public static final TFTreasureTable HEDGE_MAZE = hedgeMaze();

	public static final TFTreasureTable TOWER_ROOM = towerRoom();

	public static final TFTreasureTable TREE_CACHE = hill("tree_cache");

	public static final TFTreasureTable UNDERHILL_ROOM = underhillRoom();

	public static final TFTreasureTable UNDERHILL_DEADEND = underhillDeadEnd();

	private static final TFTreasureTable[] ALL = {
		HILL_1, HILL_2, HILL_3, HEDGE_MAZE, TOWER_ROOM, TREE_CACHE, UNDERHILL_ROOM, UNDERHILL_DEADEND,
	};

	private static final int COMMON_ROLLS = 4;
	private static final int UNCOMMON_ROLLS = 2;
	private static final int RARE_ROLLS = 1;

	private static final int SLOT_ATTEMPTS = 100;

	public static boolean place(World world, Random rand, int x, int y, int z, TFTreasureTable table) {
		if (world.getBlockId(x, y, z) != 0) {
			return false;
		}

		world.setBlockWithNotify(x, y, z, Blocks.CHEST_PLANKS_OAK.id());

		TileEntity tileEntity = world.getTileEntity(new TilePos(x, y, z));
		if (!(tileEntity instanceof Container chest)) {

			TwilightForest.LOGGER.error(
				"Placed a {} chest at {},{},{} but it has no container; it will be empty.",
				table.getName(), x, y, z);
			return true;
		}

		for (int i = 0; i < COMMON_ROLLS; i++) {
			addItemToChest(chest, rand, table.getCommonItem(rand));
		}
		for (int i = 0; i < UNCOMMON_ROLLS; i++) {
			addItemToChest(chest, rand, table.getUncommonItem(rand));
		}
		for (int i = 0; i < RARE_ROLLS; i++) {
			addItemToChest(chest, rand, table.getRareItem(rand));
		}
		return true;
	}

	private static void addItemToChest(Container chest, Random rand, ItemStack stack) {
		if (stack == null) {
			return;
		}
		for (int i = 0; i < SLOT_ATTEMPTS; i++) {
			int slot = rand.nextInt(chest.getContainerSize());
			if (chest.getItem(slot) == null) {
				chest.setItem(slot, stack);
				return;
			}
		}
	}

	public static void verify() {
		int broken = 0;
		for (TFTreasureTable table : ALL) {
			if (!table.isComplete()) {
				TwilightForest.LOGGER.error("Treasure table {} has an empty tier and will produce "
					+ "chests with missing items.", table.getName());
				broken++;
			}
		}
		if (broken == 0) {
			TwilightForest.LOGGER.info("Loaded {} Twilight Forest treasure tables.", ALL.length);
		}
	}

	private static void fillUseless(TFTreasureTable table) {
		table.useless.add(Blocks.FLOWER_RED, 4);
		table.useless.add(Blocks.FLOWER_YELLOW, 4);
		table.useless.add(Items.FEATHER_CHICKEN, 3);
		table.useless.add(Items.SEEDS_WHEAT, 2);
		table.useless.add(Items.FLINT, 2);
		table.useless.add(Blocks.CACTUS, 2);
		table.useless.add(Items.SUGARCANE, 4);
		table.useless.add(Blocks.SAND, 4);
	}

	private static TFTreasureTable hill(String name) {
		TFTreasureTable table = new TFTreasureTable(name);
		fillUseless(table);

		table.common.add(Items.INGOT_IRON, 4);
		table.common.add(Items.WHEAT, 4);
		table.common.add(Items.STRING, 4);
		table.common.add(Items.BUCKET_IRON, 1);

		table.uncommon.add(Items.FOOD_BREAD, 1);
		table.uncommon.add(Items.GUNPOWDER, 4);
		table.uncommon.add(Items.AMMO_ARROW, 12);
		table.uncommon.add(Blocks.TORCH_COAL, 12);

		table.rare.add(Items.INGOT_GOLD, 3);
		table.rare.add(Items.TOOL_PICKAXE_IRON, 1);
		table.rare.add(Items.SADDLE, 1);

		table.ultrarare.add(Items.TOOL_COMPASS, 1);
		table.ultrarare.add(Items.RECORD_CAT, 1);
		table.ultrarare.add(Items.DIAMOND, 1);
		return table;
	}

	private static TFTreasureTable hedgeMaze() {
		TFTreasureTable table = new TFTreasureTable("hedge_maze");
		fillUseless(table);

		table.common.add(Blocks.PLANKS_OAK, 4);
		table.common.add(Blocks.MUSHROOM_BROWN, 4);
		table.common.add(Blocks.MUSHROOM_RED, 4);
		table.common.add(Items.WHEAT, 4);
		table.common.add(Items.STRING, 4);
		table.common.add(Items.STICK, 6);

		table.uncommon.add(Items.FOOD_APPLE, 4);
		table.uncommon.add(Items.SEEDS_WHEAT, 4);
		table.uncommon.add(Items.SEEDS_PUMPKIN, 4);
		table.uncommon.add(Items.AMMO_ARROW, 12);
		table.uncommon.add(TFBlocks.FIREFLY, 4);

		table.rare.add(Blocks.COBWEB, 3);
		table.rare.add(Items.TOOL_SHEARS, 1);
		table.rare.add(Items.SADDLE, 1);
		table.rare.add(Items.TOOL_BOW, 1);
		table.rare.add(Items.FOOD_APPLE, 2);

		table.ultrarare.add(Items.TOOL_HOE_DIAMOND, 1);
		table.ultrarare.add(Items.DIAMOND, 1);
		table.ultrarare.add(Items.FOOD_STEW_MUSHROOM, 1);
		table.ultrarare.add(Items.FOOD_APPLE_GOLD, 1);
		return table;
	}

	private static TFTreasureTable underhillRoom() {
		TFTreasureTable table = new TFTreasureTable("underhill_room");
		fillUseless(table);

		table.common.add(Items.INGOT_IRON, 4);
		table.common.add(Items.FOOD_BREAD, 1);
		table.common.add(Items.WHEAT, 6);
		table.common.add(Items.GUNPOWDER, 4);
		table.common.add(Items.ARMOR_LEGGINGS_LEATHER, 1);
		table.common.add(Items.ARMOR_HELMET_LEATHER, 1);
		table.common.add(Items.ARMOR_BOOTS_LEATHER, 1);
		table.common.add(Items.ARMOR_CHESTPLATE_LEATHER, 1);

		table.uncommon.add(Items.ARMOR_LEGGINGS_IRON, 1);
		table.uncommon.add(Items.ARMOR_HELMET_IRON, 1);
		table.uncommon.add(Items.ARMOR_BOOTS_IRON, 1);
		table.uncommon.add(Items.ARMOR_CHESTPLATE_IRON, 1);
		table.uncommon.add(Items.TOOL_SWORD_IRON, 1);
		table.uncommon.add(Items.TOOL_AXE_IRON, 1);
		table.uncommon.add(Items.TOOL_BOW, 1);

		table.rare.add(Items.DUST_REDSTONE, 6);
		table.rare.add(Items.DUST_GLOWSTONE, 4);
		table.rare.add(Blocks.TNT, 3);

		table.rare.add(Items.FOOD_PORKCHOP_COOKED, 1);

		table.ultrarare.add(Items.SADDLE, 1);
		table.ultrarare.add(Items.BOOK, 1);
		table.ultrarare.add(Items.PAINTING, 1);
		table.ultrarare.add(Items.FOOD_APPLE_GOLD, 1);
		table.ultrarare.add(Items.RECORD_CAT, 1);
		return table;
	}

	private static TFTreasureTable underhillDeadEnd() {
		TFTreasureTable table = new TFTreasureTable("underhill_deadend");
		fillUseless(table);

		table.common.add(Items.STICK, 12);
		table.common.add(Items.COAL, 12);
		table.common.add(Items.AMMO_ARROW, 12);
		table.common.add(Items.WHEAT, 4);

		table.uncommon.add(Items.GUNPOWDER, 4);
		table.uncommon.add(Blocks.PLANKS_OAK, 6);
		table.uncommon.add(Items.LEATHER, 4);
		table.uncommon.add(Items.STRING, 4);
		table.uncommon.add(Items.PAPER, 3);
		table.uncommon.add(Items.FOOD_BREAD, 1);

		table.rare.add(Items.INGOT_IRON, 3);
		table.rare.add(Items.DUST_REDSTONE, 6);
		table.rare.add(Items.DUST_GLOWSTONE, 4);

		table.ultrarare.add(Items.BOOK, 1);
		table.ultrarare.add(Items.INGOT_IRON, 10);
		table.ultrarare.add(Items.FOOD_COOKIE, 1);
		return table;
	}

	private static TFTreasureTable towerRoom() {
		TFTreasureTable table = new TFTreasureTable("tower_room");
		fillUseless(table);

		table.common.add(Items.AMMO_SNOWBALL, 6);
		table.common.add(Items.PAPER, 4);
		table.common.add(Items.AMMO_ARROW, 12);
		table.common.add(Items.FEATHER_CHICKEN, 11);

		table.uncommon.add(Items.TOOL_SWORD_GOLD, 1);
		table.uncommon.add(Items.TOOL_PICKAXE_GOLD, 1);
		table.uncommon.add(Blocks.TORCH_REDSTONE_ACTIVE, 4);
		table.uncommon.add(Items.STRING, 4);
		table.uncommon.add(Items.BOOK, 1);
		table.uncommon.add(Items.FOOD_BREAD, 1);

		table.rare.add(Items.SLIMEBALL, 3);
		table.rare.add(Items.DUST_REDSTONE, 6);
		table.rare.add(Items.TOOL_COMPASS, 1);

		table.ultrarare.add(Items.OLIVINE, 1);
		table.ultrarare.add(Blocks.OBSIDIAN, 4);
		table.ultrarare.add(Items.DIAMOND, 1);
		return table;
	}
}
