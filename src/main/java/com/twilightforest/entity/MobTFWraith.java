package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobFlying;
import net.minecraft.core.entity.monster.Enemy;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.joml.primitives.AABBd;

public class MobTFWraith extends MobFlying implements Enemy {

	public int courseChangeCooldown = 0;
	public double waypointX;
	public double waypointY;
	public double waypointZ;

	private Entity targetedEntity = null;
	private int aggroCooldown = 0;

	public int prevAttackCounter = 0;
	public int attackCounter = 0;

	private final int attackStrength;

	public MobTFWraith(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "ghost-crown");
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;

		this.mobDrops.add(new WeightedRandomLootObject(Items.DUST_GLOWSTONE.getDefaultStack(), 1, 1));
	}

	@Override
	public int getMaxHealth() {
		return 20;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.wraith.wraith";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.wraith.wraith";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.wraith.wraith";
	}

	@Override
	protected void updateAI() {
		if (!this.world.isClientSide && !this.world.getDifficulty().canHostileMobsSpawn()) {
			this.remove();
			return;
		}

		this.tryToDespawn();
		this.prevAttackCounter = this.attackCounter;

		double dx = this.waypointX - this.x;
		double dy = this.waypointY - this.y;
		double dz = this.waypointZ - this.z;

		double dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);

		if (dist < 1.0 || dist > 60.0) {
			this.waypointX = this.x + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
			this.waypointY = this.y + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
			this.waypointZ = this.z + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
		}

		if (this.courseChangeCooldown-- <= 0) {
			this.courseChangeCooldown = this.courseChangeCooldown + this.random.nextInt(5) + 2;

			if (this.isCourseTraversable(dist)) {
				this.xd += dx / dist * 0.1;
				this.yd += dy / dist * 0.1;
				this.zd += dz / dist * 0.1;
			} else {
				this.waypointX = this.x;
				this.waypointY = this.y;
				this.waypointZ = this.z;

				this.targetedEntity = null;
			}
		}

		if (this.targetedEntity != null && this.targetedEntity.removed) {
			this.targetedEntity = null;
		}

		if (this.targetedEntity != null && this.aggroCooldown-- > 0) {
			float enemyDist = this.targetedEntity.distanceTo(this);
			if (this.canEntityBeSeen(this.targetedEntity)) {
				this.attackEntity(this.targetedEntity, enemyDist);
			}
		} else {
			this.targetedEntity = this.findPlayerToAttack();
			if (this.targetedEntity != null) {
				this.aggroCooldown = 20;
			}
		}

		double maxAttackDist = 64.0;
		if (this.targetedEntity != null
			&& this.targetedEntity.distanceToSqr(this) < maxAttackDist * maxAttackDist) {

			double tx = this.targetedEntity.x - this.x;
			double tz = this.targetedEntity.z - this.z;

			this.yBodyRot = this.yRot = -((float) Math.atan2(tx, tz)) * 180.0F / (float) Math.PI;

			if (this.canEntityBeSeen(this.targetedEntity)) {
				this.attackCounter++;
				if (this.attackCounter == 20) {

					this.waypointX = this.targetedEntity.x;
					this.waypointY = this.targetedEntity.y - this.targetedEntity.bbHeight + 0.5;
					this.waypointZ = this.targetedEntity.z;
					this.attackCounter = -40;
				}
			} else if (this.attackCounter > 0) {
				this.attackCounter--;
			}
		} else {
			this.yBodyRot = this.yRot = -((float) Math.atan2(this.xd, this.zd)) * 180.0F / (float) Math.PI;
			if (this.attackCounter > 0) {
				this.attackCounter--;
			}
		}
	}

	protected void attackEntity(Entity target, float distance) {
		if (this.attackTime <= 0 && distance < 2.0F
			&& target.bb.maxY() > this.bb.minY() && target.bb.minY() < this.bb.maxY()) {
			this.attackTime = 20;
			target.hurt(this, this.attackStrength, DamageType.COMBAT);
		}
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		if (super.hurt(attacker, damage, type)) {
			if (this.passenger != attacker && this.vehicle != attacker && attacker != this) {
				this.targetedEntity = attacker;
			}
			return true;
		}
		return false;
	}

	protected Entity findPlayerToAttack() {
		Player player = this.world.getClosestPlayerToEntity(this, 16.0);
		return player != null && this.canEntityBeSeen(player) ? player : null;
	}

	private boolean isCourseTraversable(double dist) {
		double sx = (this.waypointX - this.x) / dist;
		double sy = (this.waypointY - this.y) / dist;
		double sz = (this.waypointZ - this.z) / dist;

		AABBd probe = new AABBd(this.bb);

		for (int i = 1; i < dist; i++) {
			probe.translate(sx, sy, sz);
			if (!this.world.getCubes(this, probe).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HILL_3);
	}
}
