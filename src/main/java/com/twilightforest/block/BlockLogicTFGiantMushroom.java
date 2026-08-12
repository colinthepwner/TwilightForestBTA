package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockLogicTFGiantMushroom extends BlockLogic {

	public static final int PORES = 0;

	public static final int CAP = 5;

	public static final int CAP_DOMED = 11;

	public static final int STEM = 10;

	public static final int ALL_SKIN = 14;

	public static final int FACE_TOP = 1;
	public static final int FACE_BOTTOM = 1 << 1;
	public static final int FACE_NORTH = 1 << 2;
	public static final int FACE_SOUTH = 1 << 3;
	public static final int FACE_WEST = 1 << 4;
	public static final int FACE_EAST = 1 << 5;

	@NotNull
	private final Supplier<Block<?>> drop;

	public BlockLogicTFGiantMushroom(@NotNull Block<?> block, @NotNull Supplier<Block<?>> drop) {
		super(block, Materials.WOOD);
		this.drop = drop;
	}

	public static int skinnedFaces(int data) {
		return switch (data) {
			case 1 -> FACE_TOP | FACE_WEST | FACE_NORTH;
			case 2 -> FACE_TOP | FACE_NORTH;
			case 3 -> FACE_TOP | FACE_NORTH | FACE_EAST;
			case 4 -> FACE_TOP | FACE_WEST;
			case CAP -> FACE_TOP;
			case 6 -> FACE_TOP | FACE_EAST;
			case 7 -> FACE_TOP | FACE_SOUTH | FACE_WEST;
			case 8 -> FACE_TOP | FACE_SOUTH;
			case 9 -> FACE_TOP | FACE_SOUTH | FACE_EAST;

			case CAP_DOMED -> FACE_TOP | FACE_NORTH | FACE_SOUTH | FACE_WEST | FACE_EAST;
			case STEM -> FACE_NORTH | FACE_SOUTH | FACE_WEST | FACE_EAST;
			case ALL_SKIN -> FACE_TOP | FACE_BOTTOM | FACE_NORTH | FACE_SOUTH | FACE_WEST | FACE_EAST;
			default -> 0;
		};
	}

	@Nullable
	@Override
	public ItemStack[] getBreakResult(@NotNull World world, @NotNull EnumDropCause dropCause, int data,
									  @Nullable TileEntity tileEntity) {
		if (dropCause == EnumDropCause.SILK_TOUCH || dropCause == EnumDropCause.PICK_BLOCK) {
			return new ItemStack[]{new ItemStack(this.block)};
		}

		int count = Math.max(0, world.rand.nextInt(10) - 7);
		return count == 0 ? null : new ItemStack[]{new ItemStack(this.drop.get(), count)};
	}
}
