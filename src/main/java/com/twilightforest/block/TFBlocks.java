package com.twilightforest.block;

import com.twilightforest.block.entity.TileEntityTFBossSpawner;
import com.twilightforest.block.entity.TileEntityTFCicada;
import com.twilightforest.block.entity.TileEntityTFFirefly;
import com.twilightforest.block.entity.TileEntityTFReverter;
import com.twilightforest.block.entity.TileEntityTFTowerBuilder;
import com.twilightforest.TwilightForest;
import com.twilightforest.world.TFDimension;
import com.twilightforest.world.feature.WorldFeatureTFMinersTree;
import com.twilightforest.world.feature.WorldFeatureTFSortingTree;
import com.twilightforest.world.feature.WorldFeatureTFTreeOfTime;
import com.twilightforest.world.feature.WorldFeatureTFTreeOfTransformation;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicLeavesBase;
import net.minecraft.core.block.BlockLogicLog;
import net.minecraft.core.block.BlockLogicPortal;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.BlockSounds;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.helper.BlockBuilder;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class TFBlocks {
	private TFBlocks() {}

	public static Block<?> LOG_TWILIGHT_OAK;
	public static Block<?> LOG_CANOPY;
	public static Block<?> LOG_MANGROVE;

	public static Block<?> LEAVES_TWILIGHT_OAK;
	public static Block<?> LEAVES_CANOPY;
	public static Block<?> LEAVES_MANGROVE;

	public static Block<?> LOG_DARKWOOD;

	public static Block<?> LEAVES_DARKWOOD;
	public static Block<?> LEAVES_RAINBOW;
	public static Block<?> ROOTS;

	public static Block<?> TORCHBERRIES;

	public static Block<?> ROOT_STRANDS;

	public static Block<?> LOG_TIMEWOOD;
	public static Block<?> LOG_TRANSWOOD;
	public static Block<?> LOG_MINEWOOD;
	public static Block<?> LOG_SORTINGWOOD;

	public static Block<?> LEAVES_TIMEWOOD;
	public static Block<?> LEAVES_TRANSFORMATION;
	public static Block<?> LEAVES_MINERS;
	public static Block<?> LEAVES_SORTING;

	public static Block<?> SAPLING_TIMEWOOD;
	public static Block<?> SAPLING_TRANSFORMATION;
	public static Block<?> SAPLING_MINERS;
	public static Block<?> SAPLING_SORTING;

	public static Block<?> TOWER_WOOD;
	public static Block<?> TOWER_WOOD_ENCASED;
	public static Block<?> TOWER_WOOD_CRACKED;
	public static Block<?> TOWER_WOOD_MOSSY;

	public static Block<?> TOWER_WOOD_INFESTED;

	public static Block<?> TOWER_DEVICE;

	public static Block<?> TOWER_TRANSLUCENT;

	public static Block<?> MAZESTONE;
	public static Block<?> MAZESTONE_COBBLE;
	public static Block<?> MAZESTONE_MOSSY;

	public static Block<?> HEDGE;
	public static Block<?> FIREFLY;
	public static Block<?> CICADA;
	public static Block<?> BOSS_SPAWNER;

	public static Block<?> MUSHROOM_GIANT_BROWN;
	public static Block<?> MUSHROOM_GIANT_RED;

	public static Block<BlockLogicPortal> PORTAL_TWILIGHT;

	private static final int PORTAL_ID = 2300;

	private static CreativeInventoryPlacement after(@NotNull Supplier<IItemConvertible> neighbour) {
		return new CreativeInventoryPlacement.After(neighbour);
	}

	public static void register() {
		BlockBuilder builder = new BlockBuilder(TwilightForest.MOD_ID);

		BlockBuilder log = builder.clone()
			.setHardness(2.0f)
			.setBlockSound(BlockSounds.WOOD)
			.setTags(BlockTags.MINEABLE_BY_AXE);

		LOG_TWILIGHT_OAK = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.twilight_oak", 2301, BlockLogicLog::new);

		LOG_CANOPY = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.canopy", 2302, BlockLogicLog::new);

		LOG_MANGROVE = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.mangrove", 2303, BlockLogicLog::new);

		BlockBuilder leaves = builder.clone()
			.setHardness(0.2f)
			.setBlockSound(BlockSounds.GRASS)
			.setTags(BlockTags.MINEABLE_BY_SHEARS);

		LEAVES_TWILIGHT_OAK = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.twilight_oak", 2304, TFBlocks::leaves);

		LEAVES_CANOPY = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.canopy", 2305, TFBlocks::leaves);

		LEAVES_MANGROVE = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.mangrove", 2306, TFBlocks::leaves);

		BlockBuilder mazestone = builder.clone()
			.setHardness(20.0f)
			.setResistance(5.0f)
			.setBlockSound(BlockSounds.STONE)
			.setTags(BlockTags.MINEABLE_BY_PICKAXE);

		MAZESTONE = mazestone.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.STONE))
			.build("mazestone", 2307, block -> new BlockLogicTFMazestone(block, Blocks.STONE));

		MAZESTONE_COBBLE = mazestone.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.COBBLE_STONE))
			.build("mazestone.cobble", 2308,
				block -> new BlockLogicTFMazestone(block, Blocks.COBBLE_STONE));

		MAZESTONE_MOSSY = mazestone.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.COBBLE_STONE_MOSSY))
			.build("mazestone.mossy", 2309,
				block -> new BlockLogicTFMazestone(block, Blocks.COBBLE_STONE_MOSSY));

		HEDGE = builder.clone()
			.setHardness(2.0f)
			.setResistance(10.0f)
			.setBlockSound(BlockSounds.GRASS)
			.setTags(BlockTags.MINEABLE_BY_SHEARS)
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("hedge", 2310, BlockLogicTFHedge::new);

		TileEntityTFFirefly.register();
		TileEntityTFCicada.register();

		BlockBuilder critter = builder.clone()
			.setHardness(0.0f)
			.setBlockSound(BlockSounds.GRASS)
			.setCreativeInventoryPlacement(after(() -> Blocks.TORCH_COAL));

		FIREFLY = critter.clone()
			.setLuminance(15)
			.build("firefly", 2311, block -> {
				block.withEntity(TileEntityTFFirefly::new);
				return new BlockLogicTFFirefly(block);
			});

		CICADA = critter.clone()
			.build("cicada", 2312, block -> {
				block.withEntity(TileEntityTFCicada::new);
				return new BlockLogicTFCicada(block);
			});

		TileEntityTFBossSpawner.register();
		BOSS_SPAWNER = builder.clone()
			.setHardness(20.0f)
			.setResistance(10.0f)
			.setBlockSound(BlockSounds.STONE)
			.setTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.NOT_IN_CREATIVE_MENU)
			.build("boss_spawner", 2313, block -> {

				block.withEntity(TileEntityTFBossSpawner::new);
				return new BlockLogic(block, Materials.STONE);
			});

		BlockBuilder mushroom = builder.clone()
			.setHardness(0.2f)
			.setBlockSound(BlockSounds.WOOD)
			.setTags(BlockTags.MINEABLE_BY_AXE);

		MUSHROOM_GIANT_BROWN = mushroom.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.MUSHROOM_BROWN))
			.build("mushroom_giant.brown", 2314,
				block -> new BlockLogicTFGiantMushroom(block, () -> Blocks.MUSHROOM_BROWN));

		MUSHROOM_GIANT_RED = mushroom.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.MUSHROOM_RED))
			.build("mushroom_giant.red", 2315,
				block -> new BlockLogicTFGiantMushroom(block, () -> Blocks.MUSHROOM_RED));

		LOG_DARKWOOD = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.darkwood", 2316, BlockLogicLog::new);

		LEAVES_DARKWOOD = builder.clone()
			.setHardness(2.0f)
			.setResistance(10.0f)
			.setBlockSound(BlockSounds.GRASS)
			.setTags(BlockTags.MINEABLE_BY_SHEARS)
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.darkwood", 2317, BlockLogicTFHedge::harmless);

		LEAVES_RAINBOW = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.rainbow", 2318, TFBlocks::leaves);

		ROOTS = builder.clone()
			.setHardness(2.0f)
			.setBlockSound(BlockSounds.WOOD)
			.setTags(BlockTags.MINEABLE_BY_AXE)
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("roots", 2319, BlockLogicTFRoots::new);

		LOG_TIMEWOOD = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.timewood", 2320, BlockLogicLog::new);

		LOG_TRANSWOOD = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.transwood", 2321, BlockLogicLog::new);

		LOG_MINEWOOD = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.minewood", 2322, BlockLogicLog::new);

		LOG_SORTINGWOOD = log.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LOG_OAK))
			.build("log.sortingwood", 2323, BlockLogicLog::new);

		BlockBuilder sapling = builder.clone()
			.setHardness(0.0f)
			.setBlockSound(BlockSounds.GRASS)
			.setCreativeInventoryPlacement(after(() -> Blocks.SAPLING_OAK));

		SAPLING_TIMEWOOD = sapling.clone().build("sapling.timewood", 2328,
			block -> new BlockLogicTFSapling(block, WorldFeatureTFTreeOfTime::new));

		SAPLING_TRANSFORMATION = sapling.clone().build("sapling.transformation", 2329,
			block -> new BlockLogicTFSapling(block, WorldFeatureTFTreeOfTransformation::new));

		SAPLING_MINERS = sapling.clone().build("sapling.miners", 2330,
			block -> new BlockLogicTFSapling(block, WorldFeatureTFMinersTree::new));

		SAPLING_SORTING = sapling.clone().build("sapling.sorting", 2331,
			block -> new BlockLogicTFSapling(block, WorldFeatureTFSortingTree::new));

		LEAVES_TIMEWOOD = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.timewood", 2324, block -> leaves(block, SAPLING_TIMEWOOD));

		LEAVES_TRANSFORMATION = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.transformation", 2325, block -> leaves(block, SAPLING_TRANSFORMATION));

		LEAVES_MINERS = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.miners", 2326, block -> leaves(block, SAPLING_MINERS));

		LEAVES_SORTING = leaves.clone()
			.setCreativeInventoryPlacement(after(() -> Blocks.LEAVES_OAK))
			.build("leaves.sorting", 2327, block -> leaves(block, SAPLING_SORTING));

		TORCHBERRIES = builder.clone()
			.setHardness(0.0f)
			.setLuminance(8)
			.setBlockSound(BlockSounds.GRASS)
			.setTags(BlockTags.MINEABLE_BY_SHEARS)
			.setCreativeInventoryPlacement(after(() -> Blocks.TALLGRASS))
			.build("torchberries", 2332, BlockLogicTFTorchberries::new);

		ROOT_STRANDS = builder.clone()
			.setHardness(0.0f)
			.setBlockSound(BlockSounds.GRASS)
			.setTags(BlockTags.MINEABLE_BY_SHEARS)
			.setCreativeInventoryPlacement(after(() -> Blocks.TALLGRASS))
			.build("root_strands", 2333, BlockLogicTFRootStrands::new);

		registerDarkTower(builder);

		TwilightForest.LOGGER.info("Registered 40 Twilight Forest blocks (ids 2301-2340).");
	}

	private static void registerDarkTower(@NotNull BlockBuilder builder) {

		BlockBuilder towerWood = builder.clone()
			.setHardness(40.0f)
			.setResistance(10.0f)
			.setBlockSound(BlockSounds.WOOD)
			.setFlammability(0, 1)
			.setTags(BlockTags.MINEABLE_BY_AXE)
			.setCreativeInventoryPlacement(after(() -> Blocks.PLANKS_OAK));

		TOWER_WOOD = towerWood.clone().build("tower_wood", 2334,
			block -> new BlockLogicTFTowerWood(block, BlockLogicTFTowerWood.META_PLAIN));
		TOWER_WOOD_ENCASED = towerWood.clone().build("tower_wood.encased", 2335,
			block -> new BlockLogicTFTowerWood(block, BlockLogicTFTowerWood.META_ENCASED));
		TOWER_WOOD_CRACKED = towerWood.clone().build("tower_wood.cracked", 2336,
			block -> new BlockLogicTFTowerWood(block, BlockLogicTFTowerWood.META_CRACKED));
		TOWER_WOOD_MOSSY = towerWood.clone().build("tower_wood.mossy", 2337,
			block -> new BlockLogicTFTowerWood(block, BlockLogicTFTowerWood.META_MOSSY));
		TOWER_WOOD_INFESTED = towerWood.clone().build("tower_wood.infested", 2338,
			block -> new BlockLogicTFTowerWood(block, BlockLogicTFTowerWood.META_INFESTED));

		TileEntityTFTowerBuilder.register();
		TileEntityTFReverter.register();

		TOWER_DEVICE = builder.clone()
			.setHardness(10.0f)
			.setResistance(10000.0f)
			.setBlockSound(BlockSounds.WOOD)
			.setTags(BlockTags.MINEABLE_BY_AXE)
			.setTileEntity(TileEntityTFTowerBuilder::new)
			.setCreativeInventoryPlacement(deviceMenuEntries())
			.build("tower_device", 2339, BlockLogicTFTowerDevice::new);

		TOWER_TRANSLUCENT = builder.clone()
			.setHardness(50.0f)
			.setResistance(2000.0f)
			.setBlockSound(BlockSounds.GLASS)
			.setTags(BlockTags.MINEABLE_BY_PICKAXE, BlockTags.NOT_IN_CREATIVE_MENU)
			.build("tower_translucent", 2340, BlockLogicTFTowerTranslucent::new);
	}

	private static CreativeInventoryPlacement deviceMenuEntries() {
		return after(() -> Blocks.PLANKS_OAK).setCustomSupplier(() -> {
			List<ItemStack> stacks = new ArrayList<>();
			for (int meta : new int[]{
				BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE,
				BlockLogicTFTowerDevice.META_VANISH_INACTIVE,
				BlockLogicTFTowerDevice.META_VANISH_LOCKED,
				BlockLogicTFTowerDevice.META_VANISH_UNLOCKED,
				BlockLogicTFTowerDevice.META_BUILDER_INACTIVE,
				BlockLogicTFTowerDevice.META_ANTIBUILDER,
			}) {
				stacks.add(new ItemStack(TOWER_DEVICE, 1, meta));
			}
			return stacks;
		});
	}

	public static void registerPortal() {
		PORTAL_TWILIGHT = new BlockBuilder(TwilightForest.MOD_ID)
			.setHardness(-1.0f)
			.setResistance(6000000.0f)
			.setLuminance(12)
			.setBlockSound(BlockSounds.GLASS)
			.setTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.NOT_IN_CREATIVE_MENU)
			.build("portal.twilightforest", PORTAL_ID,
				block -> new BlockLogicTFPortal(block, TFDimension.TWILIGHT_FOREST));

		TFDimension.attachPortalBlock(PORTAL_TWILIGHT);
		TwilightForest.LOGGER.info("Registered the Twilight Forest portal.");
	}

	private static BlockLogicLeavesBase leaves(Block<?> block) {
		return new BlockLogicLeavesBase(block, Materials.LEAVES, Blocks.SAPLING_OAK);
	}

	private static BlockLogicLeavesBase leaves(Block<?> block, Block<?> sapling) {
		return new BlockLogicLeavesBase(block, Materials.LEAVES, sapling);
	}
}
