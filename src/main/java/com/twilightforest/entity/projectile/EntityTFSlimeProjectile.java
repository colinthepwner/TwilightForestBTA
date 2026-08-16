package com.twilightforest.entity.projectile;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;

public class EntityTFSlimeProjectile extends EntityTFProjectile {

	private static final int DAMAGE = 8;

	public EntityTFSlimeProjectile(World world) {
		super(world);
	}

	public EntityTFSlimeProjectile(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityTFSlimeProjectile(World world, Mob owner) {
		super(world);
		this.owner = owner;
		this.moveTo(owner.x, owner.y + owner.getHeadHeight(), owner.z, owner.yRot, owner.xRot);
		this.setPos(this.x, this.y, this.z);
		this.heightOffset = 0.0F;
		this.xd = this.yd = this.zd = 0.0;
	}

	@Override
	protected void initProjectile() {
		this.damage = 0;
		this.defaultGravity = 0.006F;
		this.defaultProjectileSpeed = 0.99F;
	}

	@Override
	public void hitEntity(Entity entityHit) {
		if (entityHit instanceof Mob) {
			entityHit.hurt(this.owner, DAMAGE, DamageType.COMBAT);
		}
	}

	@Override
	public void explode() {
		for (int i = 0; i < 8; i++) {
			this.world.spawnParticle("slimechunk", this.x, this.y, this.z,
				this.random.nextGaussian() * 0.05,
				this.random.nextDouble() * 0.2,
				this.random.nextGaussian() * 0.05, 0, false);
		}

		this.world.playSoundAtEntity(null, this, "mob.slime.big", 1.0F,
			1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
	}

	@Override
	public void makeTrail() {
		for (int i = 0; i < 2; i++) {
			double px = this.x + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			double py = this.y + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			double pz = this.z + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			this.world.spawnParticle("slimechunk", px, py, pz, 0.0, 0.0, 0.0, 0, false);
		}
	}
}
