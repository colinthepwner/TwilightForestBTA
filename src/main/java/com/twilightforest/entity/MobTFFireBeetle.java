package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.List;

public class MobTFFireBeetle extends MobMonster {

	private static final int DATA_BREATHING = 17;

	private static final float BREATH_RANGE = 5.0F;

	private static final int BREATH_DURATION = 30;

	private static final float BREATH_CHANCE = 0.1F;

	private static final int WIND_UP = 5;

	private static final int BREATH_DAMAGE = 2;

	private static final int BREATH_FIRE_TICKS = 10 * 20;

	private static final double LOOK_RANGE = 30.0;

	private int breathTime;

	private double breathX;
	private double breathY;
	private double breathZ;

	public MobTFFireBeetle(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "firebeetle");
		this.setSize(1.1F, 0.75F);

		this.moveSpeed = 0.7F;
		this.attackStrength = 4;
	}

	@Override
	public int getMaxHealth() {
		return 25;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_BREATHING, (byte) 0, Byte.class);
	}

	@Override
	public float getHeadHeight() {
		return 0.25F;
	}

	public boolean isBreathing() {
		return this.entityData.getByte(DATA_BREATHING) != 0;
	}

	private void setBreathing(boolean breathing) {
		this.entityData.set(DATA_BREATHING, (byte) (breathing ? 127 : 0));
	}

	@Override
	protected boolean isMovementCeased() {
		return this.breathTime > 0;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if (this.breathTime <= 0 && distance <= BREATH_RANGE
			&& this.random.nextFloat() < BREATH_CHANCE) {
			this.startBreathing(target);
		}
		super.attackEntity(target, distance);
	}

	@Override
	protected void updateAI() {
		super.updateAI();
		if (this.breathTime > 0) {
			this.tickBreath();
		}
	}

	private void startBreathing(Entity target) {
		this.breathTime = BREATH_DURATION;
		this.breathX = target.x;
		this.breathY = target.y + target.getHeadHeight();
		this.breathZ = target.z;
		this.setBreathing(true);
	}

	private void stopBreathing() {
		this.breathTime = 0;
		this.setBreathing(false);
	}

	private void tickBreath() {
		Entity target = this.getTarget();
		if (!this.isAlive() || target == null || !target.isAlive()
			|| target.distanceTo(this) > BREATH_RANGE || !this.canEntityBeSeen(target)) {
			this.stopBreathing();
			return;
		}

		this.breathTime--;
		this.faceBreathPoint();

		this.moveForward = 0.0F;
		this.moveStrafing = 0.0F;

		if (BREATH_DURATION - this.breathTime > WIND_UP && !this.world.isClientSide) {
			Entity hit = this.getHeadLookTarget();
			if (hit != null) {
				this.doBreathAttack(hit);
			}
		}

		if (this.breathTime <= 0) {
			this.stopBreathing();
		}
	}

	private void faceBreathPoint() {
		double dx = this.breathX - this.x;
		double dz = this.breathZ - this.z;
		double dy = this.y + this.getHeadHeight() - this.breathY;
		double flat = MathHelper.sqrt(dx * dx + dz * dz);

		float wantYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float wantPitch = (float) (-(Math.atan2(dy, flat) * 180.0 / Math.PI));

		this.xRot = -updateRotation(this.xRot, wantPitch, 100.0F);
		this.yRot = updateRotation(this.yRot, wantYaw, 100.0F);

		this.yBodyRot = this.yRot;
	}

	private static float updateRotation(float from, float to, float limit) {
		float delta = MathHelper.wrapDegrees(to - from);
		if (delta > limit) {
			delta = limit;
		}
		if (delta < -limit) {
			delta = -limit;
		}
		return from + delta;
	}

	private Entity getHeadLookTarget() {
		Vector3dc look = this.getViewVector(1.0F);
		Vector3d src = new Vector3d(this.x, this.y + this.getHeadHeight(), this.z);
		Vector3d dest = new Vector3d(
			src.x + look.x() * LOOK_RANGE,
			src.y + look.y() * LOOK_RANGE,
			src.z + look.z() * LOOK_RANGE);

		AABBd sweep = MathHelper.aabbExpand(this.bb,
			look.x() * LOOK_RANGE, look.y() * LOOK_RANGE, look.z() * LOOK_RANGE, new AABBd());
		MathHelper.aabbGrow(sweep, 3.0, 3.0, 3.0, sweep);

		Entity pointed = null;
		double hitDist = 0.0;

		List<Entity> candidates = this.world.getEntitiesWithinAABBExcludingEntity(this, sweep);
		for (Entity candidate : candidates) {
			if (!candidate.isPickable() || candidate == this) {
				continue;
			}

			float border = candidate.getPickRadius();
			AABBd box = MathHelper.aabbGrow(candidate.bb, border, border, border, new AABBd());

			if (contains(box, src)) {

				pointed = candidate;
				hitDist = 0.0;
				continue;
			}

			HitResult.Clip clip = MathHelper.aabbClip(box, src, dest);
			if (clip != null) {
				double dist = src.distance(clip.location);
				if (dist < hitDist || hitDist == 0.0) {
					pointed = candidate;
					hitDist = dist;
				}
			}
		}

		return pointed;
	}

	private static boolean contains(AABBdc box, Vector3dc point) {
		return point.x() >= box.minX() && point.x() <= box.maxX()
			&& point.y() >= box.minY() && point.y() <= box.maxY()
			&& point.z() >= box.minZ() && point.z() <= box.maxZ();
	}

	public void doBreathAttack(Entity target) {
		target.hurt(this, BREATH_DAMAGE, DamageType.FIRE);
		if (target.remainingFireTicks < BREATH_FIRE_TICKS) {
			target.remainingFireTicks = BREATH_FIRE_TICKS;
			target.maxFireTicks = target.remainingFireTicks;
			target.activeFireBlock = Blocks.FIRE;
		}
	}

	@Override
	public void onLivingUpdate() {
		if (this.isBreathing()) {
			Vector3dc look = this.getViewVector(1.0F);
			double reach = 0.9;
			double px = this.x + look.x() * reach;
			double py = this.y + 0.25 + look.y() * reach;
			double pz = this.z + look.z() * reach;

			for (int i = 0; i < 2; i++) {
				double spread = 5.0 + this.random.nextDouble() * 2.5;
				double speed = 0.15 + this.random.nextDouble() * 0.15;
				double dx = (look.x() + this.random.nextGaussian() * 0.0075 * spread) * speed;
				double dy = (look.y() + this.random.nextGaussian() * 0.0075 * spread) * speed;
				double dz = (look.z() + this.random.nextGaussian() * 0.0075 * spread) * speed;
				this.world.spawnParticle("flame", px, py, pz, dx, dy, dz, 0, false);
			}

			this.world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS,
				this.x + 0.5, this.y + 0.5, this.z + 0.5, "mob.ghast.fireball",
				this.random.nextFloat() * 0.5F, this.random.nextFloat() * 0.5F);
		}

		super.onLivingUpdate();
	}
}
