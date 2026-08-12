package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.tool.ItemToolPickaxe;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogicTFMazestone extends BlockLogic {

	public static final int PICKAXE_COST = 16;

	@NotNull
	private final Block<?> mimic;

	public BlockLogicTFMazestone(@NotNull Block<?> block, @NotNull Block<?> mimic) {
		super(block, Materials.STONE);
		this.mimic = mimic;
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data,
	                                  @Nullable TileEntity tileEntity) {
		return switch (dropCause) {
			case WORLD, EXPLOSION -> new ItemStack[0];
			default -> this.mimic.getLogic().getBreakResult(world, dropCause, 0, null);
		};
	}

	@Override
	public void onHarvest(@NotNull World world, @NotNull Player player, @NotNull TilePosc tilePos, int data,
	                      @Nullable TileEntity tileEntity) {
		ItemStack held = player.inventory.getCurrentItem();
		if (held != null) {
			Item item = held.getItem();
			if (item instanceof ItemToolPickaxe) {
				held.damageItem(PICKAXE_COST, player);
			}
		}
		super.onHarvest(world, player, tilePos, data, tileEntity);
	}
}
