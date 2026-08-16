package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicPortal;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLightning;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicTFPortal extends BlockLogicPortal {

	private static final float PORTAL_HEIGHT = 0.75F;

	private static final Block<?>[] NATURE_BLOCKS = {
		Blocks.MUSHROOM_BROWN,
		Blocks.MUSHROOM_RED,
		Blocks.TALLGRASS,
		Blocks.FLOWER_RED,
		Blocks.FLOWER_YELLOW,
	};

	public BlockLogicTFPortal(@NotNull Block<?> block, @NotNull Dimension targetDimension) {

		super(block, targetDimension, Blocks.GRASS, Blocks.FLUID_WATER_STILL);
	}

	@NotNull
	@Override
	public AABBdc getBoundsFromState(@NotNull WorldSource source, @NotNull TilePosc tilePos) {
		return new AABBd(0.0, 0.0, 0.0, 1.0, PORTAL_HEIGHT, 1.0);
	}

	@Override
	public boolean isCubeShaped() {
		return false;
	}

	@Override
	public boolean isSolidRender() {
		return false;
	}

	@Nullable
	@Override
	public BlockLogicPortal.Bounds getPortalDims(@NotNull World world, @NotNull TilePosc tilePos,
	                                             boolean expectMiddle) {
		return null;
	}

	@Nullable
	@Override
	public BlockLogicPortal.Bounds getPortalDims(@NotNull World world, @NotNull TilePosc tilePos,
	                                             boolean swapOrientation, boolean expectMiddle) {
		return null;
	}

	@Override
	public boolean tryToCreatePortal(@NotNull World world, @NotNull TilePosc tilePos,
	                                 @Nullable DyeColor color) {
		return this.tryToCreatePortal(world, tilePos.x(), tilePos.y(), tilePos.z());
	}

	@NotNull
	@Override
	public DyeColor getColor(@NotNull World world, @NotNull TilePosc tilePos) {
		return DyeColor.PURPLE;
	}

	@Override
	public void setColor(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color) {

	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Entity entity) {
		if (entity.vehicle == null && entity.passenger == null) {
			entity.handlePortal(this.block.id(), DyeColor.PURPLE);
		}
	}

	@Override
	public void onNeighborChanged(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block) {
		int x = tilePos.x();
		int y = tilePos.y();
		int z = tilePos.z();
		int self = this.block.id();

		boolean good;
		if (world.getBlockId(x - 1, y, z) == self) {
			good = isGrassOrDirt(world, x + 1, y, z);
		} else if (world.getBlockId(x + 1, y, z) == self) {
			good = isGrassOrDirt(world, x - 1, y, z);
		} else {
			good = false;
		}

		if (world.getBlockId(x, y, z - 1) == self) {
			good &= isGrassOrDirt(world, x, y, z + 1);
		} else if (world.getBlockId(x, y, z + 1) == self) {
			good &= isGrassOrDirt(world, x, y, z - 1);
		} else {
			good = false;
		}

		if (!good) {
			world.setBlockWithNotify(x, y, z, Blocks.FLUID_WATER_STILL.id());
		}
	}

	@Override
	public void animationTick(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Random rand) {
		if (rand.nextInt(100) == 0) {
			world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
				tilePos.x() + 0.5, tilePos.y() + 0.5, tilePos.z() + 0.5,
				"portal.portal", 1.0F, rand.nextFloat() * 0.4F + 0.8F);
		}

		for (int i = 0; i < 4; i++) {
			double px = tilePos.x() + rand.nextFloat();
			double py = tilePos.y() + PORTAL_HEIGHT;
			double pz = tilePos.z() + rand.nextFloat();
			double xd = (rand.nextDouble() - 0.5) * 0.5;
			double yd = rand.nextDouble() * 0.25;
			double zd = (rand.nextDouble() - 0.5) * 0.5;
			world.spawnParticle("portal", px, py, pz, xd, yd, zd, 0, false);
		}
	}

	public boolean tryToCreatePortal(@NotNull World world, int x, int y, int z) {
		if (!this.isGoodPortalPool(world, x, y, z)) {
			return false;
		}

		world.entityJoinedWorld(new EntityLightning(world, x, y, z));
		this.transmuteWaterToPortal(world, x, y, z);
		return true;
	}

	public boolean isGoodPortalPool(@NotNull World world, int x, int y, int z) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (this.isGoodPortalPoolStrict(world, x + dx, y, z + dz)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isGoodPortalPoolStrict(@NotNull World world, int x, int y, int z) {
		for (int dx = 0; dx <= 1; dx++) {
			for (int dz = 0; dz <= 1; dz++) {
				if (materialAt(world, x + dx, y, z + dz) != Materials.WATER) {
					return false;
				}
				if (!materialAt(world, x + dx, y - 1, z + dz).isSolid()) {
					return false;
				}
			}
		}

		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				boolean inPool = dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1;
				if (inPool) {
					continue;
				}

				if (!isGrassOrDirt(world, x + dx, y, z + dz)) {
					return false;
				}
				if (!isNatureBlock(world, x + dx, y + 1, z + dz)) {
					return false;
				}
			}
		}
		return true;
	}

	@Nullable
	public String describePortalFailure(@NotNull World world, int x, int y, int z) {
		String best = null;
		int bestScore = -1;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				int px = x + dx;
				int pz = z + dz;

				int water = 0;
				for (int ox = 0; ox <= 1; ox++) {
					for (int oz = 0; oz <= 1; oz++) {
						if (materialAt(world, px + ox, y, pz + oz) == Materials.WATER) {
							water++;
						}
					}
				}
				if (water <= bestScore) {
					continue;
				}

				String reason = firstFailure(world, px, y, pz);
				if (reason == null) {
					return null;
				}
				bestScore = water;
				best = reason;
			}
		}
		return best;
	}

	@Nullable
	private String firstFailure(@NotNull World world, int x, int y, int z) {
		for (int dx = 0; dx <= 1; dx++) {
			for (int dz = 0; dz <= 1; dz++) {
				if (materialAt(world, x + dx, y, z + dz) != Materials.WATER) {
					return String.format("the pool is not 2x2 water: %d,%d,%d is not water",
						x + dx, y, z + dz);
				}
				if (!materialAt(world, x + dx, y - 1, z + dz).isSolid()) {
					return String.format(
						"the pool must be exactly ONE block deep: %d,%d,%d under it is not solid",
						x + dx, y - 1, z + dz);
				}
			}
		}

		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				if (dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1) {
					continue;
				}
				if (materialAt(world, x + dx, y, z + dz) != Materials.GRASS) {
					return String.format(
						"all 12 blocks ringing the pool must be grass at the water's own level: "
							+ "%d,%d,%d is not", x + dx, y, z + dz);
				}
				if (!isNatureBlock(world, x + dx, y + 1, z + dz)) {
					return String.format(
						"every one of those 12 needs a flower, mushroom or tall grass on top: "
							+ "%d,%d,%d is bare", x + dx, y + 1, z + dz);
				}
			}
		}
		return null;
	}

	public void transmuteWaterToPortal(@NotNull World world, int x, int y, int z) {
		int px = x;
		int pz = z;
		if (materialAt(world, x - 1, y, z) == Materials.WATER) {
			px = x - 1;
		}
		if (materialAt(world, px, y, z - 1) == Materials.WATER) {
			pz = z - 1;
		}

		world.noNeighborUpdate = true;
		int id = this.block.id();
		world.setBlockWithNotify(px, y, pz, id);
		world.setBlockWithNotify(px + 1, y, pz, id);
		world.setBlockWithNotify(px + 1, y, pz + 1, id);
		world.setBlockWithNotify(px, y, pz + 1, id);
		world.noNeighborUpdate = false;
	}

	public void makePortalAt(@NotNull World world, int px, int py, int pz) {
		py = Math.max(MIN_PORTAL_Y, Math.min(MAX_PORTAL_Y, py));
		py--;

		world.noNeighborUpdate = true;
		int grass = Blocks.GRASS.id();
		int dirt = Blocks.DIRT.id();
		int portal = this.block.id();

		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				boolean inPool = dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1;
				if (inPool) {
					world.setBlockWithNotify(px + dx, py - 1, pz + dz, dirt);
					world.setBlockWithNotify(px + dx, py, pz + dz, portal);
				} else {
					world.setBlockWithNotify(px + dx, py, pz + dz, grass);
				}
			}
		}

		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				for (int dy = 1; dy <= 5; dy++) {
					world.setBlockWithNotify(px + dx, py + dy, pz + dz, 0);
				}
			}
		}

		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				boolean inPool = dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1;
				if (!inPool) {
					Block<?> plant = NATURE_BLOCKS[world.rand.nextInt(NATURE_BLOCKS.length)];
					world.setBlockAndMetadataWithNotify(px + dx, py + 1, pz + dz, plant.id(), 0);
				}
			}
		}
		world.noNeighborUpdate = false;
	}

	private static final int MIN_PORTAL_Y = 30;
	private static final int MAX_PORTAL_Y = 118;

	private static boolean isNatureBlock(@NotNull World world, int x, int y, int z) {
		Material material = materialAt(world, x, y, z);
		return material == Materials.PLANT || material == Materials.LEAVES;
	}

	private static boolean isGrassOrDirt(@NotNull World world, int x, int y, int z) {
		Material material = materialAt(world, x, y, z);
		return material == Materials.GRASS || material == Materials.DIRT;
	}

	private static Material materialAt(@NotNull World world, int x, int y, int z) {
		return world.getBlockMaterial(new TilePos(x, y, z));
	}
}
