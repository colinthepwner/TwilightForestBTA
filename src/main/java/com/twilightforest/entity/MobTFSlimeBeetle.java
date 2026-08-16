package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.projectile.EntityTFSlimeProjectile;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;

public class MobTFSlimeBeetle extends MobMonster {

	private static final float ATTACK_RANGE = 10.0F;

	private static final int SETTLE_TICKS = 20;

	private static final int RELOAD = 30;

	private static final float THROW_SPEED = 0.6F;

	private static final float THROW_SPREAD = 6.0F;

	private static final double AIM_DROP = 1.1;

	private int sightTime;

	private int rangedAttackTime;

	private boolean settled;

	public MobTFSlimeBeetle(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "slimebeetle");
		this.setSize(0.9F, 1.75F);

		this.moveSpeed = 0.7F;
		this.attackStrength = 4;
	}

	@Override
	public int getMaxHealth() {
		return 25;
	}

	@Override
	public float getHeadHeight() {
		return 0.25F;
	}

	@Override
	protected void updateAI() {
		if (this.rangedAttackTime > 0) {
			this.rangedAttackTime--;
		}

		if (this.getTarget() == null) {
			this.settled = false;
			this.sightTime = 0;
		}

		super.updateAI();

		if (this.settled) {
			this.pathToEntity = null;
			this.moveForward = 0.0F;
			this.moveStrafing = 0.0F;
		}
	}

	@Override
	protected boolean isMovementCeased() {
		return this.settled;
	}

	@Override
	protected void attackBlockedEntity(Entity target, float distance) {
		this.sightTime = 0;
		this.settled = false;
		super.attackBlockedEntity(target, distance);
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		this.sightTime++;

		if (distance <= ATTACK_RANGE) {

			this.settled = this.sightTime >= SETTLE_TICKS;

			this.lookAt(target, 30.0F, 30.0F);

			if (this.rangedAttackTime <= 0) {
				this.throwSlime(target);
				this.rangedAttackTime = RELOAD;
			}
		} else {

			this.settled = false;
		}

		super.attackEntity(target, distance);
	}

	private void throwSlime(Entity target) {
		EntityTFSlimeProjectile blob = new EntityTFSlimeProjectile(this.world, this);

		double dx = target.x - this.x;
		double dy = target.y + target.getHeadHeight() - AIM_DROP - blob.y;
		double dz = target.z - this.z;
		float arc = MathHelper.sqrt(dx * dx + dz * dz) * 0.2F;

		blob.setHeading(dx, dy + arc, dz, THROW_SPEED, THROW_SPREAD);

		this.world.playSoundAtEntity(null, this, "mob.slime", 1.0F,
			1.0F / (this.random.nextFloat() * 0.4F + 0.8F));

		this.world.entityJoinedWorld(blob);
	}
}
