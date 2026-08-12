package com.twilightforest.entity;

import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;

public class EntityTFTinyFirefly extends Entity {

	private int lifeTime;

	private int halfLife;

	public float glowSize;

	public EntityTFTinyFirefly(World world) {
		super(world);
		this.setSize(0.0F, 0.0F);
		this.roll();
	}

	public EntityTFTinyFirefly(World world, double x, double y, double z) {
		super(world);
		this.setSize(0.0F, 0.0F);
		this.moveTo(x, y, z, 0.0F, 0.0F);
		this.roll();
	}

	private void roll() {
		this.lifeTime = 10 + this.random.nextInt(21);
		this.halfLife = this.lifeTime / 2;
		this.glowSize = 0.2F + this.random.nextFloat() * 0.6F;

	}

	@Override
	public void tick() {
		super.tick();
		if (this.lifeTime <= 1) {
			this.remove();
		} else {
			this.lifeTime--;
		}
	}

	public float getGlowBrightness() {
		if (this.halfLife == 0) {
			return 0.0F;
		}
		return this.lifeTime <= this.halfLife
			? (float) this.lifeTime / this.halfLife
			: 1.0F - ((float) this.lifeTime - this.halfLife) / this.halfLife;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
	}
}
