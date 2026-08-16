package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBdc;

public class BlockLogicTFRootStrands extends BlockLogic {

	public BlockLogicTFRootStrands(@NotNull Block<?> block) {
		super(block, Materials.PLANT);

		this.setBlockBounds(0.1, 0.0, 0.1, 0.9, 0.8, 0.9);
	}

	public static boolean canPlaceRootBelow(@NotNull World world, int x, int y, int z) {
		if (BlockLogicTFTorchberries.canPlaceRootBelow(world, x, y, z)) {
			return true;
		}
		return world.getBlockId(x, y, z) == TFBlocks.ROOT_STRANDS.id();
	}

	@Override
	public boolean canStay(@NotNull World world, @NotNull TilePosc tilePos) {
		return canPlaceRootBelow(world, tilePos.x(), tilePos.y() + 1, tilePos.z());
	}

	@Override
	public boolean canPlaceAt(@NotNull World world, @NotNull TilePosc tilePos) {
		return super.canPlaceAt(world, tilePos) && this.canStay(world, tilePos);
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos,
	                              @NotNull Block<?> block) {
		super.onNeighborChanged(world, tilePos, block);
		if (!this.canStay(world, tilePos)) {
			this.dropWithCause(world, EnumDropCause.WORLD, tilePos, world.getBlockData(tilePos),
				null, null);
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
		}
	}

	@Override
	public ItemStack @NotNull [] getBreakResult(@NotNull World world, @NotNull EnumDropCause cause,
	                                            int meta, @Nullable TileEntity tileEntity) {
		return switch (cause) {
			case PROPER_TOOL, SILK_TOUCH, PICK_BLOCK ->
				new ItemStack[]{new ItemStack(this.block)};
			default -> new ItemStack[0];
		};
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Nullable
	@Override
	public AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return null;
	}

	@NotNull
	@Override
	public ISupport getSupport(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side) {
		return PartialSupport.INSTANCE;
	}
}
