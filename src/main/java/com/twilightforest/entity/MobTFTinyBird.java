package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class MobTFTinyBird extends MobChicken {

	private static final int MAX_HEALTH = 1;

	private static final double TRIGGER_RANGE = 2.0;

	private static final double ESCAPE_DISTANCE = 16.0;

	private static final double SPRINT_RANGE_SQ = 49.0;

	private static final double SAFE_RANGE_SQ = 100.0;

	private static final int FLEE_TICKS = 200;

	private static final int REPATH_TICKS = 30;

	private static final int SCATTER = 8;

	private static final float PATH_SEARCH_RANGE = 16.0F;

	private static final float NORMAL_SPEED = 0.45F;
	private static final float FLEE_SPEED_FAR = 0.41F;
	private static final float FLEE_SPEED_NEAR = 0.72F;

	@Nullable
	private Entity threat;

	private int fleeTicks;

	private int repathTicks;

	public MobTFTinyBird(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "tinybird");
		this.setSize(0.3F, 0.7F);
		this.setSkinVariant(this.random.nextInt(4));
		this.moveSpeed = NORMAL_SPEED;
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected float getBlockPathWeight(@NotNull TilePosc pos) {
		return TFAmbientPathing.weigh(this.world, pos);
	}

	@Override
	protected void updateAI() {
		super.updateAI();
		this.tickFlee();
	}

	private void tickFlee() {
		if (this.threat == null) {
			Player found = this.world.getClosestPlayer(this.x, this.y, this.z, TRIGGER_RANGE);

			if (found == null || !found.getGamemode().hasHostileMobs()) return;
			this.threat = found;
			this.fleeTicks = FLEE_TICKS;
			this.repathTicks = 0;
			this.doRandomWalk = false;
		}

		if (!this.threat.isAlive()) {
			this.stopFleeing();
			return;
		}

		double distSq = this.distanceToSqr(this.threat);
		if (distSq > SAFE_RANGE_SQ) {
			if (--this.fleeTicks <= 0) {
				this.stopFleeing();
				return;
			}
		} else {
			this.fleeTicks = FLEE_TICKS;
		}

		this.moveSpeed = distSq < SPRINT_RANGE_SQ ? FLEE_SPEED_NEAR : FLEE_SPEED_FAR;

		if (--this.repathTicks <= 0 || this.pathToEntity == null || this.pathToEntity.isDone()) {
			this.repathTicks = REPATH_TICKS;
			this.pathAwayFrom(this.threat);
		}
	}

	private void stopFleeing() {
		this.threat = null;
		this.doRandomWalk = true;
		this.moveSpeed = NORMAL_SPEED;
		this.pathToEntity = null;
	}

	private void pathAwayFrom(Entity from) {
		double toX = this.x + this.random.nextInt(SCATTER) - SCATTER / 2.0;
		double toZ = this.z + this.random.nextInt(SCATTER) - SCATTER / 2.0;
		double dx = toX - from.x;
		double dz = toZ - from.z;
		if (dx * dx + dz * dz < 1.0E-4) {

			double angle = this.random.nextDouble() * Math.PI * 2.0;
			dx = Math.cos(angle);
			dz = Math.sin(angle);
		}
		Vector3d away = new Vector3d(dx, 0.0, dz).normalize().mul(ESCAPE_DISTANCE);
		this.pathToEntity = this.world.getEntityPathToTilePos(this,
			new TilePos(away.add(this.x, this.y, this.z)), PATH_SEARCH_RANGE);
	}

	@Override
	public boolean hurt(@Nullable Entity attacker, int damage, DamageType type) {
		if (attacker != null && attacker != this) {
			this.threat = attacker;
			this.fleeTicks = FLEE_TICKS;
			this.repathTicks = 0;
			this.doRandomWalk = false;
		}
		return super.hurt(attacker, damage, type);
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.chirp";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.hurt";
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
