package com.twilightforest.entity.projectile;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

public class EntityTFNatureBolt extends EntityTFProjectile {

	public EntityTFNatureBolt(World world) {
		super(world);
	}

	public EntityTFNatureBolt(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityTFNatureBolt(World world, Mob owner, double aimX, double aimY, double aimZ) {
		super(world, owner, aimX, aimY, aimZ);
	}

	@Override
	public void explode() {
		int bx = (int) this.x;
		int by = (int) this.y;
		int bz = (int) this.z;

		if (this.world.getBlockMaterial(new TilePos(bx, by, bz)).isSolid()) {
			this.world.setBlockWithNotify(bx, by, bz, Blocks.LEAVES_BIRCH.id());
		}
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
			this.world.spawnParticle("crit", px, py, pz, 0.0, 0.0, 0.0, 0, false);
		}
	}
}
