package com.twilightforest.block.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.MobTFNaga;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.collection.NamespaceID;
import org.joml.primitives.AABBd;

public class TileEntityTFBossSpawner extends TileEntityMobSpawner {

	public static final String ID = "boss_spawner";

	private static final double PLAYER_RANGE = 50.0;

	public TileEntityTFBossSpawner() {
		super();
		this.setMobId(TwilightForest.MOD_ID + ":naga");

		this.delay = 0;
	}

	public static void register() {
		net.minecraft.core.block.entity.TileEntityDispatcher.addMapping(
			TileEntityTFBossSpawner.class,
			NamespaceID.getPermanent(TwilightForest.MOD_ID, ID));
	}

	@Override
	public boolean anyPlayerInRange() {
		return this.worldObj != null && this.worldObj.getClosestPlayer(
			this.tilePos.x + 0.5, this.tilePos.y + 0.5, this.tilePos.z + 0.5, PLAYER_RANGE) != null;
	}

	@Override
	public void tick() {
		if (this.worldObj == null
			|| this.worldObj.getBlockId(this.tilePos.x, this.tilePos.y, this.tilePos.z)
			!= TFBlocks.BOSS_SPAWNER.id()) {
			return;
		}

		this.yaw2 = this.yaw;

		if (!this.anyPlayerInRange()) {
			return;
		}

		double px = this.tilePos.x + this.worldObj.rand.nextFloat();
		double py = this.tilePos.y + this.worldObj.rand.nextFloat();
		double pz = this.tilePos.z + this.worldObj.rand.nextFloat();
		this.worldObj.spawnParticle("smoke", px, py, pz, 0.0, 0.0, 0.0, 0, false);
		this.worldObj.spawnParticle("flame", px, py, pz, 0.0, 0.0, 0.0, 0, false);

		for (this.yaw = this.yaw + 1000.0F / (this.delay + 200.0F); this.yaw > 360.0; this.yaw2 -= 360.0) {
			this.yaw -= 360.0;
		}

		if (this.worldObj.isClientSide) {
			return;
		}

		if (this.delay == -1) {
			this.updateDelay();
		}

		if (this.delay > 0) {
			this.delay--;
			return;
		}

		if (!this.worldObj.getDifficulty().canHostileMobsSpawn()) {
			return;
		}

		Mob boss = (Mob) EntityDispatcher.getInstance()
			.createEntityInWorld(this.getMobId(), this.worldObj);
		if (boss == null) {
			return;
		}

		AABBd arena = new AABBd(this.tilePos.x, this.tilePos.y, this.tilePos.z,
			this.tilePos.x + 1, this.tilePos.y + 1, this.tilePos.z + 1);
		MathHelper.aabbGrow(arena, 100.0, 20.0, 100.0, arena);
		if (!this.worldObj.getEntitiesWithinAABB(boss.getClass(), arena).isEmpty()) {
			this.updateDelay();
			return;
		}

		double sx = this.tilePos.x + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 4.0;
		double sy = this.tilePos.y + this.worldObj.rand.nextInt(3) - 1;
		double sz = this.tilePos.z + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 4.0;
		boss.moveTo(sx, sy, sz, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);

		this.worldObj.entityJoinedWorld(boss);

		for (int i = 0; i < 20; i++) {
			double bx = this.tilePos.x + 0.5 + (this.worldObj.rand.nextFloat() - 0.5) * 2.0;
			double by = this.tilePos.y + 0.5 + (this.worldObj.rand.nextFloat() - 0.5) * 2.0;
			double bz = this.tilePos.z + 0.5 + (this.worldObj.rand.nextFloat() - 0.5) * 2.0;
			this.worldObj.spawnParticle("smoke", bx, by, bz, 0.0, 0.0, 0.0, 0, false);
			this.worldObj.spawnParticle("flame", bx, by, bz, 0.0, 0.0, 0.0, 0, false);
		}

		if (boss instanceof MobTFNaga naga) {
			naga.setHome(this.tilePos.x, this.tilePos.y, this.tilePos.z);
		}
		boss.spawnExplosionParticle();

		this.updateDelay();

		this.worldObj.setBlockWithNotify(this.tilePos.x, this.tilePos.y, this.tilePos.z, 0);
	}

	private void updateDelay() {
		this.delay = 200 + this.worldObj.rand.nextInt(600);
	}
}
