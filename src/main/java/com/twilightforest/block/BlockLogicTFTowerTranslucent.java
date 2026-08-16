package com.twilightforest.block;

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
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicTFTowerTranslucent extends BlockLogic {

	public static final int META_REAPPEARING_INACTIVE = 0;
	public static final int META_REAPPEARING_ACTIVE = 1;
	public static final int META_BUILT_INACTIVE = 2;
	public static final int META_BUILT_ACTIVE = 3;
	public static final int META_REVERTER_REPLACEMENT = 4;

	public static final int META_COUNT = 5;

	public static final int REAPPEAR_DELAY = 80;

	private static final float REVERTER_HARDNESS = 0.3F;

	private static final double SEED_MIN = 0.375;
	private static final double SEED_MAX = 0.625;

	public BlockLogicTFTowerTranslucent(@NotNull Block<?> block) {

		super(block, Materials.GLASS);
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlockOnCondition(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int meta = source.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		return meta == META_BUILT_INACTIVE || meta == META_BUILT_ACTIVE
			|| meta == META_REVERTER_REPLACEMENT;
	}

	@NotNull
	@Override
	public AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int meta = source.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		if (meta == META_REAPPEARING_INACTIVE || meta == META_REAPPEARING_ACTIVE) {
			return new AABBd(SEED_MIN, SEED_MIN, SEED_MIN, SEED_MAX, SEED_MAX, SEED_MAX);
		}
		return this.getBounds();
	}

	@Nullable
	@Override
	public AABBdc getCollisionAABB(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		int meta = source.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		if (meta == META_REAPPEARING_INACTIVE || meta == META_REAPPEARING_ACTIVE) {
			return null;
		}
		return super.getCollisionAABB(source, tilePos);
	}

	@Override
	public float getStrength(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Side side,
	                         @Nullable Player player) {
		float base = super.getStrength(world, tilePos, side, player);
		int meta = world.getBlockMetadata(tilePos.x(), tilePos.y(), tilePos.z());
		if (meta != META_REVERTER_REPLACEMENT) {
			return base;
		}
		return base * (this.block.blockHardness / REVERTER_HARDNESS);
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

		if (meta == META_BUILT_ACTIVE) {
			world.setBlockWithNotify(x, y, z, 0);
			world.notifyBlocksOfNeighborChange(x, y, z, this.block.id());
			playAt(world, x, y, z, "random.pop", 0.3F, 0.5F);
			world.markBlocksDirty(x, y, z, x, y, z);
			BlockLogicTFTowerDevice.activateNeighbours(world, x, y, z);
		}

		if (meta == META_REAPPEARING_ACTIVE) {
			world.setBlockAndMetadataWithNotify(x, y, z, TFBlocks.TOWER_DEVICE.id(),
				BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE);
			world.notifyBlocksOfNeighborChange(x, y, z, this.block.id());
			playAt(world, x, y, z, "random.click", 0.3F, 0.5F);
			world.markBlocksDirty(x, y, z, x, y, z);
		} else if (meta == META_REAPPEARING_INACTIVE) {
			BlockLogicTFTowerDevice.changeToActiveVanishBlock(world, x, y, z, META_REAPPEARING_ACTIVE);
		}
	}

	@Override
	public ItemStack @NotNull [] getBreakResult(@NotNull World world, @NotNull EnumDropCause cause,
	                                            int meta, @Nullable TileEntity tileEntity) {
		return new ItemStack[0];
	}

	@Override
	public String getLanguageKey(int meta) {
		return this.block.getKey() + "." + (meta >= 0 && meta < META_COUNT ? meta : 0);
	}

	private static void playAt(@NotNull World world, int x, int y, int z, @NotNull String sound,
	                           float volume, float pitch) {
		world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			x + 0.5, y + 0.5, z + 0.5, sound, volume, pitch);
	}
}
