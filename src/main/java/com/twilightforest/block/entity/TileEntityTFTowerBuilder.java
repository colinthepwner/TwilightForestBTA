package com.twilightforest.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.block.BlockLogicTFTowerDevice;
import com.twilightforest.block.BlockLogicTFTowerTranslucent;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.LevelListener;
import org.jetbrains.annotations.Nullable;

public class TileEntityTFTowerBuilder extends TileEntity {

	public static final String ID = "tower_builder";

	private static final int RANGE = 16;

	private static final int BUILD_INTERVAL = 10;

	private static final int STOP_TIMEOUT = 60;

	private static final int NO_FACING = -1;

	private int ticksRunning;
	private int blockedCounter;
	private int ticksStopped;

	public boolean makingBlocks;

	private int blocksMade;

	private boolean hasLastBlock;
	private int lastX;
	private int lastY;
	private int lastZ;

	@Nullable
	private Player trackedPlayer;

	public static void register() {
		TileEntityDispatcher.addMapping(TileEntityTFTowerBuilder.class,
			NamespaceID.getPermanent(TwilightForest.MOD_ID, ID));
	}

	public void startBuilding() {
		this.makingBlocks = true;
		this.blocksMade = 0;
		this.hasLastBlock = true;
		this.lastX = this.tilePos.x;
		this.lastY = this.tilePos.y;
		this.lastZ = this.tilePos.z;
	}

	@Override
	public void tick() {
		if (this.worldObj == null || this.worldObj.isClientSide) {
			return;
		}

		if (this.getBlockMeta() != BlockLogicTFTowerDevice.META_BUILDER_ACTIVE) {
			this.goDormant();
			return;
		}

		if (this.makingBlocks) {
			this.build();
		} else {
			this.countDownToTimeout();
		}
	}

	private void build() {
		if (this.trackedPlayer == null) {
			this.trackedPlayer = this.findClosestValidPlayer();
		}

		int nextFacing = this.findNextFacing();
		this.ticksRunning++;

		if (this.ticksRunning % BUILD_INTERVAL == 0 && this.hasLastBlock && nextFacing != NO_FACING) {
			Side side = Side.fromId(nextFacing);
			int nextX = this.lastX + side.offsetX();
			int nextY = this.lastY + side.offsetY();
			int nextZ = this.lastZ + side.offsetZ();

			if (this.blocksMade <= RANGE && this.worldObj.isAirBlock(nextX, nextY, nextZ)) {
				this.worldObj.setBlockAndMetadataWithNotify(nextX, nextY, nextZ,
					TFBlocks.TOWER_TRANSLUCENT.id(),
					BlockLogicTFTowerTranslucent.META_BUILT_INACTIVE);

				this.worldObj.playBlockEvent(nextX, nextY, nextZ,
					LevelListener.EVENT_DISPENSER_EMPTY, 0);
				this.lastX = nextX;
				this.lastY = nextY;
				this.lastZ = nextZ;
				this.blockedCounter = 0;
				this.blocksMade++;
			} else {
				this.blockedCounter++;
			}
		}

		if (this.blockedCounter > 0) {
			this.makingBlocks = false;
			this.trackedPlayer = null;
			this.ticksStopped = 0;
		}
	}

	private void countDownToTimeout() {
		this.trackedPlayer = null;
		if (++this.ticksStopped == STOP_TIMEOUT) {
			this.worldObj.setBlockMetadataWithNotify(this.tilePos.x, this.tilePos.y, this.tilePos.z,
				BlockLogicTFTowerDevice.META_BUILDER_TIMEOUT);
			this.worldObj.scheduleBlockUpdate(this.tilePos.x, this.tilePos.y, this.tilePos.z,
				TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.BUILDER_TICK);
		}
	}

	private void goDormant() {
		this.makingBlocks = false;
		this.trackedPlayer = null;
		this.ticksRunning = 0;
		this.ticksStopped = 0;
		this.blockedCounter = 0;
		this.blocksMade = 0;
		this.hasLastBlock = false;
	}

	private int findNextFacing() {
		if (this.trackedPlayer == null) {
			return NO_FACING;
		}

		int pitch = MathHelper.floor(this.trackedPlayer.xRot * 4.0F / 360.0F + 1.5) & 3;
		if (pitch == 0) {
			return Side.TOP.id;
		}
		if (pitch == 2) {
			return Side.BOTTOM.id;
		}

		int direction = MathHelper.floor(this.trackedPlayer.yRot * 4.0F / 360.0F + 0.5) & 3;
		return switch (direction) {
			case 0 -> Side.SOUTH.id;
			case 1 -> Side.WEST.id;
			case 2 -> Side.NORTH.id;
			case 3 -> Side.EAST.id;
			default -> NO_FACING;
		};
	}

	@Nullable
	private Player findClosestValidPlayer() {
		return this.worldObj.getClosestPlayer(
			this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5, RANGE);
	}

	@Override
	public void readAdditionalData(CompoundTag tag) {

	}

	@Override
	public void writeAdditionalData(CompoundTag tag) {

	}
}
