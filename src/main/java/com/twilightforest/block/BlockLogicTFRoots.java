package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockLogicTFRoots extends BlockLogic {

	public BlockLogicTFRoots(Block<?> block) {
		super(block, Materials.WOOD);
	}

	@Override
	public ItemStack @NotNull [] getBreakResult(@NotNull World world, @NotNull EnumDropCause cause,
	                                            int meta, TileEntity tileEntity) {
		return new ItemStack[]{new ItemStack(Items.STICK, 1)};
	}
}
