package com.twilightforest.block;

import com.twilightforest.block.entity.TileEntityTFReverter;
import com.twilightforest.block.entity.TileEntityTFTowerBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockLogicTFTowerDevice extends BlockLogic {

	public static final int META_REAPPEARING_INACTIVE = 0;
	public static final int META_REAPPEARING_ACTIVE = 1;
	public static final int META_VANISH_INACTIVE = 2;
	public static final int META_VANISH_ACTIVE = 3;
	public static final int META_VANISH_LOCKED = 4;
	public static final int META_VANISH_UNLOCKED = 5;
	public static final int META_BUILDER_INACTIVE = 6;
	public static final int META_BUILDER_ACTIVE = 7;
	public static final int META_BUILDER_TIMEOUT = 8;
	public static final int META_ANTIBUILDER = 9;
	public static final int META_GHASTTRAP_INACTIVE = 10;
	public static final int META_GHASTTRAP_ACTIVE = 11;
	public static final int META_REACTOR_INACTIVE = 12;
	public static final int META_REACTOR_ACTIVE = 13;

	public static final int META_COUNT = 14;

	public static final int TICK_RATE = 15;

	public static final int VANISH_TICK_MIN = 2;
	public static final int VANISH_TICK_SPREAD = 5;

	public static final int BUILT_TICK_RATE = 10;

	public static final int BUILDER_TICK = 4;

	private static final int LOCK_RANGE = 2;

	public BlockLogicTFTowerDevice(@NotNull Block<?> block) {

		super(block, Materials.WOOD);
	}

	@Override
	public boolean onInteracted(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player,
	                            @NotNull Side side, double xHit, double yHit) {
		int x = tilePos.x();
		int y = tilePos.y();
		int z = tilePos.z();
		int meta = world.getBlockMetadata(x, y, z);

		if (meta == META_VANISH_INACTIVE) {
			if (areNearbyLockBlocks(world, x, y, z)) {
				playAt(world, x, y, z, "random.click", 1.0F, 0.3F);
			} else {
				changeToActiveVanishBlock(world, x, y, z, META_VANISH_ACTIVE);
			}
			return true;
		}

		if (meta == META_REAPPEARING_INACTIVE) {
			if (areNearbyLockBlocks(world, x, y, z)) {
				playAt(world, x, y, z, "random.click", 1.0F, 0.3F);
			} else {
				changeToActiveVanishBlock(world, x, y, z, META_REAPPEARING_ACTIVE);
			}
			return true;
		}

		return false;
	}

	@Override
	public float getStrength(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
	                         @Nullable Player player) {
		int meta = world.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		if (meta >= META_REAPPEARING_ACTIVE && meta <= META_VANISH_UNLOCKED) {
			return 0.0F;
		}
		return super.getStrength(world, tilePos, side, player);
	}

	public static boolean areNearbyLockBlocks(@NotNull World world, int x, int y, int z) {
		boolean locked = false;
		int deviceId = TFBlocks.TOWER_DEVICE.id();
		for (int dx = x - LOCK_RANGE; dx <= x + LOCK_RANGE; dx++) {
			for (int dy = y - LOCK_RANGE; dy <= y + LOCK_RANGE; dy++) {
				for (int dz = z - LOCK_RANGE; dz <= z + LOCK_RANGE; dz++) {
					if (world.getBlockId(dx, dy, dz) == deviceId
						&& world.getBlockMetadata(dx, dy, dz) == META_VANISH_LOCKED) {
						locked = true;
					}
				}
			}
		}
		return locked;
	}

	public static void unlockBlock(@NotNull World world, int x, int y, int z) {
		if (world.getBlockId(x, y, z) == TFBlocks.TOWER_DEVICE.id()
			&& world.getBlockMetadata(x, y, z) == META_VANISH_LOCKED) {
			changeToBlockMeta(world, x, y, z, META_VANISH_UNLOCKED);
			playAt(world, x, y, z, "random.click", 0.3F, 0.6F);
		}
	}

	private static void changeToBlockMeta(@NotNull World world, int x, int y, int z, int meta) {
		int thereId = world.getBlockId(x, y, z);
		if (thereId == TFBlocks.TOWER_DEVICE.id() || thereId == TFBlocks.TOWER_TRANSLUCENT.id()) {
			world.setBlockMetadataWithNotify(x, y, z, meta);
		}
	}

	public static void changeToActiveVanishBlock(@NotNull World world, int x, int y, int z, int meta) {
		changeToBlockMeta(world, x, y, z, meta);
		playAt(world, x, y, z, "random.pop", 0.3F, 0.6F);

		int thereId = world.getBlockId(x, y, z);
		world.scheduleBlockUpdate(x, y, z, thereId, getTickRateFor(thereId, meta, world.rand));
	}

	private static int getTickRateFor(int blockId, int meta, @NotNull Random rand) {
		if (blockId == TFBlocks.TOWER_DEVICE.id()
			&& (meta == META_VANISH_ACTIVE || meta == META_REAPPEARING_ACTIVE)) {
			return VANISH_TICK_MIN + rand.nextInt(VANISH_TICK_SPREAD);
		}
		if (blockId == TFBlocks.TOWER_TRANSLUCENT.id()
			&& meta == BlockLogicTFTowerTranslucent.META_BUILT_ACTIVE) {
			return BUILT_TICK_RATE;
		}
		return TICK_RATE;
	}

	public static void checkAndActivateVanishBlock(@NotNull World world, int x, int y, int z) {
		int thereId = world.getBlockId(x, y, z);
		int thereMeta = world.getBlockMetadata(x, y, z);
		int deviceId = TFBlocks.TOWER_DEVICE.id();

		if (thereId == deviceId
			&& (thereMeta == META_VANISH_INACTIVE || thereMeta == META_VANISH_UNLOCKED)
			&& !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_VANISH_ACTIVE);
		} else if (thereId == deviceId && thereMeta == META_REAPPEARING_INACTIVE
			&& !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_REAPPEARING_ACTIVE);
		} else if (thereId == TFBlocks.TOWER_TRANSLUCENT.id()
			&& thereMeta == BlockLogicTFTowerTranslucent.META_BUILT_INACTIVE) {
			changeToActiveVanishBlock(world, x, y, z, BlockLogicTFTowerTranslucent.META_BUILT_ACTIVE);
		}
	}

	static void activateNeighbours(@NotNull World world, int x, int y, int z) {
		checkAndActivateVanishBlock(world, x - 1, y, z);
		checkAndActivateVanishBlock(world, x + 1, y, z);
		checkAndActivateVanishBlock(world, x, y + 1, z);
		checkAndActivateVanishBlock(world, x, y - 1, z);
		checkAndActivateVanishBlock(world, x, y, z + 1);
		checkAndActivateVanishBlock(world, x, y, z - 1);
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onPlacedByWorld(world, tilePos);
		int x = tilePos.x();
		int y = tilePos.y();
		int z = tilePos.z();
		int meta = world.getBlockMetadata(x, y, z);

		fixTileEntity(world, tilePos, meta);

		if (!world.isClientSide && meta == META_BUILDER_INACTIVE && world.hasNeighborSignal(x, y, z)) {
			changeToBlockMeta(world, x, y, z, META_BUILDER_ACTIVE);
			playAt(world, x, y, z, "random.click", 0.3F, 0.6F);
		}
	}

	@Override
	public void onChunkLoad(@NotNull World world, @NotNull TilePosc tilePos) {
		super.onChunkLoad(world, tilePos);
		fixTileEntity(world, tilePos,
			world.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z()));
	}

	private static void fixTileEntity(@NotNull World world, @NotNull TilePosc tilePos, int meta) {
		if (meta != META_ANTIBUILDER) {
			return;
		}
		TileEntity there = world.getTileEntity(tilePos);
		if (!(there instanceof TileEntityTFReverter)) {
			world.setTileEntity(tilePos, new TileEntityTFReverter());
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		super.onNeighborChanged(world, tilePos, block);
		if (world.isClientSide) {
			return;
		}

		int x = tilePos.x();
		int y = tilePos.y();
		int z = tilePos.z();
		int meta = world.getBlockMetadata(x, y, z);
		boolean powered = world.hasNeighborSignal(x, y, z);

		if (meta == META_VANISH_INACTIVE && powered && !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_VANISH_ACTIVE);
		}
		if (meta == META_REAPPEARING_INACTIVE && powered && !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_REAPPEARING_ACTIVE);
		}
		if (meta == META_BUILDER_INACTIVE && powered) {
			changeToBlockMeta(world, x, y, z, META_BUILDER_ACTIVE);
			playAt(world, x, y, z, "random.click", 0.3F, 0.6F);
			world.scheduleBlockUpdate(x, y, z, this.block.id(), BUILDER_TICK);
		}
		if (meta == META_BUILDER_ACTIVE && !powered) {
			changeToBlockMeta(world, x, y, z, META_BUILDER_INACTIVE);
			playAt(world, x, y, z, "random.click", 0.3F, 0.6F);
			world.scheduleBlockUpdate(x, y, z, this.block.id(), BUILDER_TICK);
		}
		if (meta == META_BUILDER_TIMEOUT && !powered) {
			changeToBlockMeta(world, x, y, z, META_BUILDER_INACTIVE);
		}
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand,
	                       boolean randomTick) {

		if (world.isClientSide || randomTick) {
			return;
		}

		int x = tilePos.x();
		int y = tilePos.y();
		int z = tilePos.z();
		int meta = world.getBlockMetadata(x, y, z);

		if (meta == META_VANISH_ACTIVE || meta == META_REAPPEARING_ACTIVE) {
			if (meta == META_VANISH_ACTIVE) {
				world.setBlockWithNotify(x, y, z, 0);
			} else {
				int translucentId = TFBlocks.TOWER_TRANSLUCENT.id();
				world.setBlockAndMetadataWithNotify(x, y, z, translucentId,
					BlockLogicTFTowerTranslucent.META_REAPPEARING_INACTIVE);
				world.scheduleBlockUpdate(x, y, z, translucentId,
					BlockLogicTFTowerTranslucent.REAPPEAR_DELAY);
			}
			world.notifyBlocksOfNeighborChange(x, y, z, this.block.id());
			playAt(world, x, y, z, "random.pop", 0.3F, 0.5F);
			world.markBlocksDirty(x, y, z, x, y, z);
			activateNeighbours(world, x, y, z);
		}

		if (meta == META_BUILDER_ACTIVE && world.hasNeighborSignal(x, y, z)) {
			letsBuild(world, tilePos);
		}

		if (meta == META_BUILDER_INACTIVE || meta == META_BUILDER_TIMEOUT) {
			activateNeighbours(world, x, y, z);
		}
	}

	private static void letsBuild(@NotNull World world, @NotNull TilePosc tilePos) {
		if (world.getTileEntity(tilePos) instanceof TileEntityTFTowerBuilder builder
			&& !builder.makingBlocks) {
			builder.startBuilding();
		}
	}

	@Override
	public ItemStack @NotNull [] getBreakResult(@NotNull World world, @NotNull EnumDropCause cause,
	                                            int meta, @Nullable TileEntity tileEntity) {
		if (meta == META_ANTIBUILDER && cause != EnumDropCause.PICK_BLOCK) {
			return new ItemStack[0];
		}
		return new ItemStack[]{new ItemStack(this.block, 1, damageDropped(meta))};
	}

	public static int damageDropped(int meta) {
		return switch (meta) {
			case META_REAPPEARING_ACTIVE -> META_REAPPEARING_INACTIVE;
			case META_VANISH_ACTIVE -> META_VANISH_INACTIVE;
			case META_BUILDER_ACTIVE, META_BUILDER_TIMEOUT -> META_BUILDER_INACTIVE;
			case META_GHASTTRAP_ACTIVE -> META_GHASTTRAP_INACTIVE;
			case META_REACTOR_ACTIVE -> META_REACTOR_INACTIVE;
			default -> meta;
		};
	}

	@Override
	public int getPlacedData(@NotNull Player player, @NotNull ItemStack stack, @NotNull World world,
	                         @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {
		return stack.getMetadata();
	}

	@Override
	public String getLanguageKey(int meta) {
		return this.block.getKey() + "." + (meta >= 0 && meta < META_COUNT ? meta : 0);
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		int meta = world.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		if (meta == META_REAPPEARING_ACTIVE || meta == META_VANISH_ACTIVE || meta == META_BUILDER_ACTIVE) {
			sparkle(world, tilePos.x(), tilePos.y(), tilePos.z(), rand);
		}
		fixTileEntity(world, tilePos, meta);
	}

	private static void sparkle(@NotNull World world, int x, int y, int z, @NotNull Random rand) {
		final double offset = 0.0625;
		for (int side = 0; side < 6; side++) {
			double rx = x + rand.nextFloat();
			double ry = y + rand.nextFloat();
			double rz = z + rand.nextFloat();

			if (side == 0 && !world.isBlockOpaqueCube(x, y + 1, z)) {
				ry = (y + 1) + offset;
			}
			if (side == 1 && !world.isBlockOpaqueCube(x, y - 1, z)) {
				ry = y - offset;
			}
			if (side == 2 && !world.isBlockOpaqueCube(x, y, z + 1)) {
				rz = (z + 1) + offset;
			}
			if (side == 3 && !world.isBlockOpaqueCube(x, y, z - 1)) {
				rz = z - offset;
			}
			if (side == 4 && !world.isBlockOpaqueCube(x + 1, y, z)) {
				rx = (x + 1) + offset;
			}
			if (side == 5 && !world.isBlockOpaqueCube(x - 1, y, z)) {
				rx = x - offset;
			}

			if (rx < x || rx > x + 1 || ry < 0.0 || ry > y + 1 || rz < z || rz > z + 1) {
				world.spawnParticle("reddust", rx, ry, rz, 0.0, 0.0, 0.0, 0, false);
			}
		}
	}

	private static void playAt(@NotNull World world, int x, int y, int z, @NotNull String sound,
	                           float volume, float pitch) {
		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			x + 0.5, y + 0.5, z + 0.5, sound, volume, pitch);
	}
}
