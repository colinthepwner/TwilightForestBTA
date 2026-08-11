package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.monster.Enemy;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.joml.primitives.AABBd;

public class MobTFNagaSegment extends Mob implements Enemy {

	private static final double SEGMENT_DISTANCE = 1.5;

	private static final int DATA_SEGMENT = 16;

	private int deathCounter;

	private final int attackStrength = 3;

	MobTFNaga head;

	public MobTFNagaSegment(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "nagasegment");
		this.setSize(1.75F, 1.95F);
		this.footSize = 2.0F;
		this.fireImmune = true;

		this.blocksBuilding = true;
	}

	public MobTFNagaSegment(World world, MobTFNaga head, int segment) {
		this(world);
		this.blocksBuilding = false;
		this.head = head;
		this.setSegment(segment);
		this.moveSpeed = head.chaseSpeed() * 1.5F;
		this.speed = head.speed;
		this.flySpeed = head.flySpeed;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SEGMENT, (byte) 1, Byte.class);
	}

	public void setSegment(int segment) {
		this.entityData.set(DATA_SEGMENT, (byte) segment);
	}

	public int getSegment() {
		return this.entityData.getByte(DATA_SEGMENT);
	}

	@Override
	public int getMaxHealth() {
		return 250;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void causeFallDamage(float distance) {
	}

	@Override
	public void knockBack(Entity attacker, int damage, double dx, double dz) {
	}

	@Override
	public void tick() {
		this.despawnIfInvalid();

		if (this.deathCounter > 0) {
			this.deathCounter--;
			if (this.deathCounter == 0) {
				for (int i = 0; i < 20; i++) {
					double vx = this.random.nextGaussian() * 0.02;
					double vy = this.random.nextGaussian() * 0.02;
					double vz = this.random.nextGaussian() * 0.02;
					String explosion = this.random.nextBoolean() ? "largeexplode" : "explode";
					this.world.spawnParticle(explosion,
						this.x + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
						this.y + this.random.nextFloat() * this.bbHeight,
						this.z + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
						vx, vy, vz, 0, false);
				}
				this.remove();
			}
		}

		super.tick();
	}

	@Override
	protected void updateAI() {
		if (this.head == null || this.head.attackTime > 0) {
			return;
		}

		AABBd touching = MathHelper.aabbGrow(this.bb, 0.75, 0.75, 0.75, new AABBd());

		for (Entity near : this.world.getEntitiesWithinAABBExcludingEntity(this, touching)) {
			if (near instanceof Mob && !(near instanceof MobTFNaga) && !(near instanceof MobTFNagaSegment)) {
				this.head.attackTime = 10;
				this.dealDamage(near);
			}
		}
	}

	public boolean dealDamage(Entity target) {
		int damage = target instanceof Player ? this.attackStrength * 3 : this.attackStrength;
		return target.hurt(this, damage, DamageType.COMBAT);
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		if (type == DamageType.FIRE || type == DamageType.BLAST) {
			this.remainingFireTicks = 0;
			return false;
		}

		if (this.world.isClientSide || this.deathCounter > 0) {
			return false;
		}

		this.hurtTime = this.maxHurtTime = 10;
		return this.head != null
			&& this.head.hurt(attacker, Math.round(damage * 2.0F / 3.0F), type);
	}

	public void despawnIfInvalid() {
		if (this.world.isClientSide) {
			return;
		}

		if (this.head == null || this.head.removed) {
			this.remove();
		}

		if (!this.world.getDifficulty().canHostileMobsSpawn()) {
			this.remove();
		}
	}

	public void pullTowards(Entity leader) {
		if (this.head != null) {
			this.moveSpeed = this.head.chaseSpeed() * 1.5F;
		}

		float angle = (float) Math.atan2(this.z - leader.z, this.x - leader.x);
		double idealX = leader.x + MathHelper.cos(angle) * SEGMENT_DISTANCE;
		double idealZ = leader.z + MathHelper.sin(angle) * SEGMENT_DISTANCE;

		double dx = idealX - this.x;
		double dy = leader.y - this.y;
		double dz = idealZ - this.z;
		double dist = MathHelper.sqrt(dx * dx + dz * dz);

		if (dist > SEGMENT_DISTANCE * 4.0) {

			this.absMoveTo(idealX, leader.y + 0.001, idealZ,
				0.0F, (float) (angle * 180.0F / Math.PI) - 90.0F);
			dy = 0.0;
			this.lookAt(leader, 90.0F, 90.0F);

			this.moveForward = (float) (this.moveSpeed * dist * 5.0);
		} else if (dist > 0.0) {
			this.lookAt(leader, 90.0F, 90.0F);
			this.moveForward = this.head != null ? this.head.chaseSpeed() : this.moveSpeed;
		}

		if (dy > 1.1) {
			this.jump();
		}
	}

	public void selfDestruct() {
		this.hurtTime = this.maxHurtTime = 30;
		this.deathCounter = 30;
	}
}
