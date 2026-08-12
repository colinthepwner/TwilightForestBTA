package com.twilightforest.block.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.collection.NamespaceID;

public class TileEntityTFCicada extends TileEntity {

	public static final String ID = "cicada";

	private static final String SONG = TwilightForest.MOD_ID + ":mob.tf.cicada";

	private static final int SONG_LENGTH = 100;

	public int yawDelay;
	public int currentYaw;
	public int desiredYaw;
	public int singDuration;
	public boolean singing;
	public int singDelay;

	public static void register() {
		TileEntityDispatcher.addMapping(TileEntityTFCicada.class,
			NamespaceID.getPermanent(TwilightForest.MOD_ID, ID));
	}

	@Override
	public void tick() {
		if (this.worldObj == null) {
			return;
		}
		this.tickYaw();
		this.tickSong();
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

	private void tickSong() {
		if (this.singDelay > 0) {
			this.singDelay--;
			return;
		}

		if (this.singing && this.singDuration == 0) {
			this.playSong();
		}
		if (this.singing && this.singDuration >= SONG_LENGTH) {
			this.singing = false;
			this.singDuration = 0;
		}
		if (this.singing && this.singDuration < SONG_LENGTH) {
			this.singDuration++;
			this.doSingAnimation();
		}
		if (!this.singing && this.singDuration <= 0) {
			this.singing = true;
			this.singDelay = SONG_LENGTH + this.worldObj.rand.nextInt(SONG_LENGTH);
		}
	}

	public void doSingAnimation() {
		if (this.worldObj.rand.nextInt(5) != 0) {
			return;
		}
		double rx = this.tilePos.x + this.worldObj.rand.nextFloat();
		double ry = this.tilePos.y + this.worldObj.rand.nextFloat();
		double rz = this.tilePos.z + this.worldObj.rand.nextFloat();
		this.worldObj.spawnParticle("note", rx, ry, rz, 0.0, 0.0, 0.0, 0, false);
	}

	public void playSong() {
		float pitch = (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F + 1.0F;
		this.worldObj.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
			this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5, SONG, 1.0F, pitch);
	}

	@Override
	public void readAdditionalData(CompoundTag tag) {

	}

	@Override
	public void writeAdditionalData(CompoundTag tag) {

	}
}
