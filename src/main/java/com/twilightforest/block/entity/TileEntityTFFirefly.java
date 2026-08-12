package com.twilightforest.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.entity.EntityTFTinyFirefly;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.util.collection.NamespaceID;

public class TileEntityTFFirefly extends TileEntity {

	public static final String ID = "firefly";

	private static final double PLAYER_RANGE = 16.0;

	public int yawDelay;
	public int currentYaw;
	public int desiredYaw;
	public float glowIntensity;
	public boolean glowing;
	public int glowDelay;

	public static void register() {
		TileEntityDispatcher.addMapping(TileEntityTFFirefly.class,
			NamespaceID.getPermanent(TwilightForest.MOD_ID, ID));
	}

	@Override
	public void tick() {
		if (this.worldObj == null) {
			return;
		}

		if (this.anyPlayerInRange() && this.worldObj.rand.nextInt(20) == 0) {
			this.doFireflyFX();
		}

		this.tickYaw();
		this.tickGlow();
	}

	private void tickYaw() {
		if (this.yawDelay > 0) {
			this.yawDelay--;
			return;
		}

		if (this.currentYaw == 0 && this.desiredYaw == 0) {
			this.yawDelay = 200 + this.worldObj.rand.nextInt(200);

			this.desiredYaw = this.worldObj.rand.nextInt(15) - this.worldObj.rand.nextInt(15);
		}

		if (this.currentYaw < this.desiredYaw) {
			this.currentYaw++;
		}
		if (this.currentYaw > this.desiredYaw) {
			this.currentYaw--;
		}
		if (this.currentYaw == this.desiredYaw) {
			this.desiredYaw = 0;
		}
	}

	private void tickGlow() {
		if (this.glowDelay > 0) {
			this.glowDelay--;
			return;
		}

		if (this.glowing && this.glowIntensity >= 1.0F) {
			this.glowing = false;
		}
		if (this.glowing && this.glowIntensity < 1.0F) {
			this.glowIntensity = (float) (this.glowIntensity + 0.05);
		}
		if (!this.glowing && this.glowIntensity > 0.0F) {
			this.glowIntensity = (float) (this.glowIntensity - 0.05);
		}
		if (!this.glowing && this.glowIntensity <= 0.0F) {
			this.glowing = true;
			this.glowDelay = this.worldObj.rand.nextInt(50);
		}
	}

	public boolean anyPlayerInRange() {
		return this.worldObj.getClosestPlayer(
			this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5, PLAYER_RANGE) != null;
	}

	private void doFireflyFX() {
		double rx = this.tilePos.x + this.worldObj.rand.nextFloat();
		double ry = this.tilePos.y + this.worldObj.rand.nextFloat();
		double rz = this.tilePos.z + this.worldObj.rand.nextFloat();
		this.worldObj.entityJoinedWorld(new EntityTFTinyFirefly(this.worldObj, rx, ry, rz));
	}

	@Override
	public void readAdditionalData(CompoundTag tag) {

	}

	@Override
	public void writeAdditionalData(CompoundTag tag) {

	}
}
