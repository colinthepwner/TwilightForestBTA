package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class MobTFTowerBroodling extends MobTFSwarmSpider {

	private static final int MAX_HEALTH = 7;

	private static final int BASE_ATTACK = 3;

	private static final int GROUND_DAMAGE = 2;

	private static final int LEAP_DAMAGE = 4;

	private static final int LEAP_BONUS_ONE_IN = 2;

	public MobTFTowerBroodling(World world) {
		this(world, true);
	}

	public MobTFTowerBroodling(World world, boolean spawnMore) {
		super(world, spawnMore);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "towerbroodling");
		this.attackStrength = BASE_ATTACK;
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected MobTFSwarmSpider createSibling() {
		return new MobTFTowerBroodling(this.world, false);
	}

	@Override
	protected void attackEntity(@NotNull Entity entity, float distance) {
		this.attackStrength = !this.onGround && this.random.nextInt(LEAP_BONUS_ONE_IN) == 0
			? LEAP_DAMAGE : GROUND_DAMAGE;
		super.attackEntity(entity, distance);
	}
}
