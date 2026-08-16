package com.twilightforest.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.block.BlockLogicTFTowerTranslucent;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TileEntityTFReverter extends TileEntity {

	public static final String ID = "tower_reverter";

	private static final int REVERT_CHANCE = 10;

	private static final int RADIUS = 4;
	private static final int DIAMETER = 2 * RADIUS + 1;

	private static final double REQUIRED_PLAYER_RANGE = 16.0;

	private static final int SLOW_SCAN_INTERVAL = 20;

	private static final int QUIET_SWEEPS_BEFORE_SLOWING = 20;

	private static final int LINE_PARTICLES = 16;

	private static final int OUTLINE_COUNT = 12;

	private final Random rand = new Random();

	private int tickCount;
	private boolean slowScan;
	private int ticksSinceChange;

	@Nullable
	private short[] blockData;
	@Nullable
	private byte[] metaData;

	public static void register() {
		TileEntityDispatcher.addMapping(TileEntityTFReverter.class,
			NamespaceID.getPermanent(TwilightForest.MOD_ID, ID));
	}

	@Override
	public void tick() {
		if (this.worldObj == null) {
			return;
		}

		if (!this.anyPlayerInRange()) {

			this.blockData = null;
			this.metaData = null;
			this.tickCount = 0;
			return;
		}

		this.tickCount++;

		if (this.worldObj.isClientSide) {
			this.showOff();
			return;
		}

		if (this.blockData == null || this.metaData == null) {
			this.captureBlockData();
			this.slowScan = true;
		}

		if (!this.slowScan || this.tickCount % SLOW_SCAN_INTERVAL == 0) {
			if (this.scanAndRevertChanges()) {
				this.slowScan = false;
				this.ticksSinceChange = 0;
			} else if (++this.ticksSinceChange > QUIET_SWEEPS_BEFORE_SLOWING) {
				this.slowScan = true;
			}
		}
	}

	private void showOff() {
		this.worldObj.spawnParticle("reddust",
			this.tilePos.x + this.worldObj.rand.nextFloat(),
			this.tilePos.y + this.worldObj.rand.nextFloat(),
			this.tilePos.z + this.worldObj.rand.nextFloat(),
			0.0, 0.0, 0.0, 0, false);

		if (this.rand.nextInt(REVERT_CHANCE) == 0) {
			this.makeOutline(this.rand.nextInt(OUTLINE_COUNT));
			this.makeOutline(this.rand.nextInt(OUTLINE_COUNT));
			this.makeOutline(this.rand.nextInt(OUTLINE_COUNT));
		}
	}

	private void makeOutline(int outline) {
		double sx = this.tilePos.x;
		double sy = this.tilePos.y;
		double sz = this.tilePos.z;
		double dx = this.tilePos.x;
		double dy = this.tilePos.y;
		double dz = this.tilePos.z;

		switch (outline) {
			case 0, 8 -> {
				sx -= RADIUS;
				dx += RADIUS + 1;
				sz -= RADIUS;
				dz -= RADIUS;
			}
			case 1, 9 -> {
				sx -= RADIUS;
				dx -= RADIUS;
				sz -= RADIUS;
				dz += RADIUS + 1;
			}
			case 2, 10 -> {
				sx -= RADIUS;
				dx += RADIUS + 1;
				sz += RADIUS + 1;
				dz += RADIUS + 1;
			}
			case 3, 11 -> {
				sx += RADIUS + 1;
				dx += RADIUS + 1;
				sz -= RADIUS;
				dz += RADIUS + 1;
			}
			case 4 -> {
				sx -= RADIUS;
				dx -= RADIUS;
				sz -= RADIUS;
				dz -= RADIUS;
			}
			case 5 -> {
				sx += RADIUS + 1;
				dx += RADIUS + 1;
				sz -= RADIUS;
				dz -= RADIUS;
			}
			case 6 -> {
				sx += RADIUS + 1;
				dx += RADIUS + 1;
				sz += RADIUS + 1;
				dz += RADIUS + 1;
			}
			case 7 -> {
				sx -= RADIUS;
				dx -= RADIUS;
				sz += RADIUS + 1;
				dz += RADIUS + 1;
			}
			default -> {  }
		}

		switch (outline) {
			case 0, 1, 2, 3 -> {
				sy += RADIUS + 1;
				dy += RADIUS + 1;
			}
			case 4, 5, 6, 7 -> {
				sy -= RADIUS;
				dy += RADIUS + 1;
			}
			case 8, 9, 10, 11 -> {
				sy -= RADIUS;
				dy -= RADIUS;
			}
			default -> {  }
		}

		if (this.rand.nextBoolean()) {
			this.drawParticleLine(this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5,
				dx, dy, dz);
		} else {
			this.drawParticleLine(sx, sy, sz,
				this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5);
		}
		this.drawParticleLine(sx, sy, sz, dx, dy, dz);
	}

	private void drawParticleLine(double srcX, double srcY, double srcZ,
	                              double destX, double destY, double destZ) {
		for (int i = 0; i < LINE_PARTICLES; i++) {
			double t = (double) i / (LINE_PARTICLES - 1.0);
			double tx = srcX + (destX - srcX) * t + this.rand.nextFloat() * 0.005;
			double ty = srcY + (destY - srcY) * t + this.rand.nextFloat() * 0.005;
			double tz = srcZ + (destZ - srcZ) * t + this.rand.nextFloat() * 0.005;
			this.worldObj.spawnParticle("reddust", tx, ty, tz, 0.0, 0.0, 0.0, 0, false);
		}
	}

	private void captureBlockData() {
		this.blockData = new short[DIAMETER * DIAMETER * DIAMETER];
		this.metaData = new byte[DIAMETER * DIAMETER * DIAMETER];
		int index = 0;
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					this.blockData[index] = (short) this.worldObj.getBlockId(
						this.tilePos.x + x, this.tilePos.y + y, this.tilePos.z + z);
					this.metaData[index] = (byte) this.worldObj.getBlockMetadata(
						this.tilePos.x + x, this.tilePos.y + y, this.tilePos.z + z);
					index++;
				}
			}
		}
	}

	private boolean scanAndRevertChanges() {
		if (this.blockData == null || this.metaData == null) {
			return false;
		}
		int index = 0;
		boolean reverted = false;
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					int wx = this.tilePos.x + x;
					int wy = this.tilePos.y + y;
					int wz = this.tilePos.z + z;
					short thereId = (short) this.worldObj.getBlockId(wx, wy, wz);
					byte thereMeta = (byte) this.worldObj.getBlockMetadata(wx, wy, wz);

					if (this.blockData[index] != thereId) {
						if (this.revertBlock(wx, wy, wz, thereId, thereMeta,
							this.blockData[index], this.metaData[index])) {
							reverted = true;
						} else {

							this.blockData[index] = thereId;
							this.metaData[index] = thereMeta;
						}
					}
					index++;
				}
			}
		}
		return reverted;
	}

	private boolean revertBlock(int x, int y, int z, short thereId, byte thereMeta,
	                            short replaceId, byte replaceMeta) {

		if (thereId == 0) {
			Block<?> replaced = Blocks.getBlock(replaceId);
			if (replaced != null
				&& replaced.renderAsNormalBlockOnCondition(this.worldObj, new TilePos(x, y, z))) {
				return false;
			}
		}

		if (isUnrevertable(thereId, thereMeta, replaceId, replaceMeta)) {
			return false;
		}

		if (this.rand.nextInt(REVERT_CHANCE) == 0) {
			short writeId = replaceId;
			byte writeMeta = replaceMeta;
			if (writeId != 0) {

				writeId = (short) TFBlocks.TOWER_TRANSLUCENT.id();
				writeMeta = (byte) BlockLogicTFTowerTranslucent.META_REVERTER_REPLACEMENT;
			}

			this.worldObj.setBlockAndMetadataWithNotify(x, y, z, writeId, writeMeta);

			if (thereId == 0) {

				this.worldObj.playBlockEvent(x, y, z, LevelListener.EVENT_BLOCK_BREAK, writeId);
			} else if (replaceId == 0) {

				this.worldObj.playBlockEvent(x, y, z, LevelListener.EVENT_BLOCK_BREAK, thereId);
				Block<?> placed = Blocks.getBlock(thereId);
				if (placed != null) {
					placed.dropWithCause(this.worldObj, EnumDropCause.WORLD, new TilePos(x, y, z),
						thereMeta, null, null);
				}
			}
		}
		return true;
	}

	private static boolean isUnrevertable(short thereId, byte thereMeta,
	                                      short replaceId, byte replaceMeta) {
		int device = TFBlocks.TOWER_DEVICE.id();
		int translucent = TFBlocks.TOWER_TRANSLUCENT.id();
		int antiBuilt = BlockLogicTFTowerTranslucent.META_REVERTER_REPLACEMENT;

		if (thereId == device || replaceId == device) {
			return true;
		}
		if ((thereId == translucent && thereMeta != antiBuilt)
			|| (replaceId == translucent && replaceMeta != antiBuilt)) {
			return true;
		}
		if (isLampPair(thereId, replaceId)) {
			return true;
		}
		if (thereId == Blocks.FLUID_WATER_FLOWING.id() || replaceId == Blocks.FLUID_WATER_FLOWING.id()) {
			return true;
		}
		if (thereId == Blocks.FLUID_WATER_STILL.id() || replaceId == Blocks.FLUID_WATER_STILL.id()) {
			return true;
		}
		return replaceId == Blocks.TNT.id();
	}

	private static boolean isLampPair(int a, int b) {
		return (a == Blocks.LAMP_IDLE.id() && b == Blocks.LAMP_ACTIVE.id())
			|| (a == Blocks.LAMP_ACTIVE.id() && b == Blocks.LAMP_IDLE.id())
			|| (a == Blocks.LAMP_INVERTED_IDLE.id() && b == Blocks.LAMP_INVERTED_ACTIVE.id())
			|| (a == Blocks.LAMP_INVERTED_ACTIVE.id() && b == Blocks.LAMP_INVERTED_IDLE.id());
	}

	public boolean anyPlayerInRange() {
		return this.worldObj.getClosestPlayer(
			this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5,
			REQUIRED_PLAYER_RANGE) != null;
	}

	@Override
	public void readAdditionalData(CompoundTag tag) {

	}

	@Override
	public void writeAdditionalData(CompoundTag tag) {

	}
}
