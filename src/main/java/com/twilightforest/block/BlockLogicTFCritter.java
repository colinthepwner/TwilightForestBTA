package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicTorch;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public abstract class BlockLogicTFCritter extends BlockLogicTorch {

	public static final int SIDE_CEILING = 6;

	protected BlockLogicTFCritter(@NotNull Block<?> block) {
		super(block);
	}

	protected abstract boolean canAttachTo(@NotNull World world, int x, int y, int z);

	@Nullable
	private static TilePos supportOf(@NotNull TilePosc tilePos, int orientation) {
		return switch (orientation) {
			case SIDE_WEST -> new TilePos(tilePos.x() - 1, tilePos.y(), tilePos.z());
			case SIDE_EAST -> new TilePos(tilePos.x() + 1, tilePos.y(), tilePos.z());
			case SIDE_NORTH -> new TilePos(tilePos.x(), tilePos.y(), tilePos.z() - 1);
			case SIDE_SOUTH -> new TilePos(tilePos.x(), tilePos.y(), tilePos.z() + 1);
			case SIDE_BOTTOM -> new TilePos(tilePos.x(), tilePos.y() - 1, tilePos.z());
			case SIDE_CEILING -> new TilePos(tilePos.x(), tilePos.y() + 1, tilePos.z());
			default -> null;
		};
	}

	private boolean canAttachTo(@NotNull World world, @Nullable TilePos pos) {
		return pos != null && this.canAttachTo(world, pos.x, pos.y, pos.z);
	}

	@NotNull
	@Override
	public AABBdc getBoundsFromState(@NotNull WorldSource world, @NotNull TilePosc tilePos) {
		int orientation = world.getBlockData(tilePos) & MASK_DIRECTION;
		float f = 0.15F;
		return switch (orientation) {
			case SIDE_WEST -> new AABBd(0.0, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
			case SIDE_EAST -> new AABBd(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0, 0.8F, 0.5F + f);
			case SIDE_NORTH -> new AABBd(0.5F - f, 0.2F, 0.0, 0.5F + f, 0.8F, f * 2.0F);
			case SIDE_SOUTH -> new AABBd(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0);
			case SIDE_BOTTOM -> new AABBd(0.5F - f, 0.0, 0.2F, 0.5F + f, f * 2.0F, 0.8F);
			case SIDE_CEILING -> new AABBd(0.5F - f, 1.0F - f * 2.0F, 0.2F, 0.5F + f, 1.0, 0.8F);
			default -> new AABBd(0.4F, 0.0, 0.4F, 0.6F, 0.6F, 0.6F);
		};
	}

	@Override
	public boolean canPlaceAt(@NotNull World world, @NotNull TilePosc tilePos) {
		for (int orientation = SIDE_WEST; orientation <= SIDE_CEILING; orientation++) {
			if (this.canAttachTo(world, supportOf(tilePos, orientation))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getPlacedData(@Nullable Player player, @NotNull ItemStack itemStack, @NotNull World world,
	                         @NotNull TilePosc tilePos, @NotNull Side side, double xHit, double yHit) {

		if (player == null && side.isHorizontal()) {
			side = side.opposite();
		}

		int orientation = switch (side) {
			case TOP -> SIDE_BOTTOM;
			case BOTTOM -> SIDE_CEILING;
			case NORTH -> SIDE_SOUTH;
			case SOUTH -> SIDE_NORTH;
			case WEST -> SIDE_EAST;
			case EAST -> SIDE_WEST;
			default -> SIDE_NONE;
		};

		if (orientation != SIDE_NONE && !this.canAttachTo(world, supportOf(tilePos, orientation))) {
			orientation = SIDE_NONE;
		}
		return orientation == SIDE_NONE ? this.getDefaultOrientation(world, tilePos) : orientation;
	}

	@Override
	public int getDefaultOrientation(@NotNull World world, @NotNull TilePosc tilePos) {
		for (int orientation = SIDE_WEST; orientation <= SIDE_CEILING; orientation++) {
			if (this.canAttachTo(world, supportOf(tilePos, orientation))) {
				return orientation;
			}
		}
		return SIDE_NONE;
	}

	@Override
	public void onPlacedByWorld(@NotNull World world, @NotNull TilePosc tilePos) {
		if (world.getBlockData(tilePos) == 0) {
			int orientation = this.getDefaultOrientation(world, tilePos);
			if (orientation != SIDE_NONE) {
				world.setBlockDataNotify(tilePos, orientation);
			}
		}
		this.dropCritterIfCantStay(world, tilePos);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		this.dropCritterIfCantStay(world, tilePos);
	}

	private void dropCritterIfCantStay(@NotNull World world, @NotNull TilePosc tilePos) {
		int data = world.getBlockData(tilePos);
		int orientation = data & MASK_DIRECTION;
		if (this.canAttachTo(world, supportOf(tilePos, orientation))) {
			return;
		}
		this.dropWithCause(world, EnumDropCause.WORLD, tilePos, data, null, null);
		world.setBlockTypeNotify(tilePos, Blocks.AIR);
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {

	}
}
