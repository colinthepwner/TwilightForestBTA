package com.twilightforest.entity.projectile;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;

public class EntityTFLichBolt extends EntityTFProjectile {

	public EntityTFLichBolt(World world) {
		super(world);
	}

	public EntityTFLichBolt(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityTFLichBolt(World world, Mob owner, double aimX, double aimY, double aimZ) {
		super(world, owner, aimX, aimY, aimZ);
	}

	@Override
	public void explode() {
	}

	@Override
	public void hitEntity(Entity entityHit) {
		entityHit.hurt(this.owner, 1, DamageType.COMBAT);
	}

	@Override
	public void makeTrail() {
		for (int i = 0; i < 5; i++) {
			double px = this.x + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			double py = this.y + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			double pz = this.z + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
			double sparkle = 0.5;
			double dx = (this.random.nextFloat() - 0.5F) * sparkle;
			double dy = (this.random.nextFloat() - 0.5F) * sparkle;
			double dz = (this.random.nextFloat() - 0.5F) * sparkle;
			this.world.spawnParticle("mobSpell", px, py, pz, dx, dy, dz, 0, false);
		}
	}
}
