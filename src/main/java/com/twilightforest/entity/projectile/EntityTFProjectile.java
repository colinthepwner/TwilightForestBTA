package com.twilightforest.entity.projectile;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.projectile.Projectile;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class EntityTFProjectile extends Projectile {

	public double accelerationX;
	public double accelerationY;
	public double accelerationZ;

	public EntityTFProjectile(World world) {
		super(world);
		this.setSize(1.0F, 1.0F);
	}

	public EntityTFProjectile(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(1.0F, 1.0F);
		this.moveTo(x, y, z, this.yRot, this.xRot);
		this.setPos(x, y, z);
	}

	public EntityTFProjectile(World world, double x, double y, double z,
	                          double aimX, double aimY, double aimZ) {
		super(world, x, y, z);
		this.setSize(1.0F, 1.0F);
		this.moveTo(x, y, z, this.yRot, this.xRot);
		this.setPos(x, y, z);
		this.aimAt(aimX, aimY, aimZ);
	}

	public EntityTFProjectile(World world, Mob owner, double aimX, double aimY, double aimZ) {
		super(world, owner.x, owner.y + owner.getHeadHeight(), owner.z);
		this.setSize(1.0F, 1.0F);
		this.owner = owner;
		this.moveTo(owner.x, owner.y + owner.getHeadHeight(), owner.z, owner.yRot, owner.xRot);
		this.setPos(this.x, this.y, this.z);
		this.heightOffset = 0.0F;
		this.xd = this.yd = this.zd = 0.0;

		this.aimAt(
			aimX + this.random.nextGaussian() * 0.4,
			aimY + this.random.nextGaussian() * 0.4,
			aimZ + this.random.nextGaussian() * 0.4
		);
	}

	private void aimAt(double aimX, double aimY, double aimZ) {
		double length = Math.sqrt(aimX * aimX + aimY * aimY + aimZ * aimZ);
		this.accelerationX = aimX / length * 0.1;
		this.accelerationY = aimY / length * 0.1;
		this.accelerationZ = aimZ / length * 0.1;
	}

	@Override
	protected void initProjectile() {
		this.damage = 0;
		this.defaultGravity = 0.0F;
		this.defaultProjectileSpeed = 0.95F;
	}

	@Override
	public void onHit(@NotNull HitResult hitResult) {
		if (!this.world.isClientSide) {
			if (hitResult instanceof HitResult.Entity hit) {
				this.hitEntity(hit.entity);
			}
			this.explode();
		}
		this.remove();
	}

	public void explode() {
	}

	public void hitEntity(Entity entityHit) {
	}

	@Override
	public void afterTick() {
		this.xd += this.accelerationX;
		this.yd += this.accelerationY;
		this.zd += this.accelerationZ;
		super.afterTick();
		this.makeTrail();
	}

	public void makeTrail() {
		this.world.spawnParticle("smoke", this.x, this.y + 0.5, this.z, 0.0, 0.0, 0.0, 0, false);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public float getPickRadius() {
		return 1.0F;
	}

	@Override
	public float getShadowHeightOffs() {
		return 0.0F;
	}
}
