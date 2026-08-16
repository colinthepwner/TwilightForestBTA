package com.twilightforest.block;

import com.twilightforest.entity.MobTFTowerTermite;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogicTFTowerWood extends BlockLogic {

	public static final int META_PLAIN = 0;
	public static final int META_ENCASED = 1;
	public static final int META_CRACKED = 2;
	public static final int META_MOSSY = 3;

	public static final int META_INFESTED = 4;

	private static final float INFESTED_HARDNESS = 0.75F;

	private final int species;

	public BlockLogicTFTowerWood(@NotNull Block<?> block, int species) {

		super(block, Materials.WOOD);
		this.species = species;
	}

	public int species() {
		return this.species;
	}

	@Override
	public float getStrength(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
	                         @Nullable Player player) {
		float base = super.getStrength(world, tilePos, side, player);
		if (this.species != META_INFESTED) {
			return base;
		}
		return base * (this.block.blockHardness / INFESTED_HARDNESS);
	}

	@Override
	public ItemStack @NotNull [] getBreakResult(@NotNull World world, @NotNull EnumDropCause cause,
	                                            int meta, @Nullable TileEntity tileEntity) {
		if (this.species == META_INFESTED && cause != EnumDropCause.PICK_BLOCK) {
			return new ItemStack[0];
		}
		return new ItemStack[]{new ItemStack(this.block)};
	}

	@Override
	public void onDestroyedByPlayer(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
	                                int data, @Nullable Player player, @Nullable Item item) {
		if (!world.isClientSide && this.species == META_INFESTED) {
			MobTFTowerTermite.spawnFromBlock(world, tilePos.x(), tilePos.y(), tilePos.z());
		}
		super.onDestroyedByPlayer(world, tilePos, side, data, player, item);
	}
}
