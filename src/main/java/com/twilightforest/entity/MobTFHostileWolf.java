package com.twilightforest.entity;

import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.world.World;

public class MobTFHostileWolf extends MobWolf {

	private static final int HEALTH = 10;

	public MobTFHostileWolf(World world) {
		super(world);
		this.setWolfAngry(true);
		this.setHealthRaw(HEALTH);
	}

	@Override
	public int getMaxHealth() {
		return HEALTH;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isClientSide && !this.world.getDifficulty().canHostileMobsSpawn()) {
			this.remove();
		}
	}
}
