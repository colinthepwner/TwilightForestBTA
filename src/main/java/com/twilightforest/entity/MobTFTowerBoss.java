package com.twilightforest.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.world.World;

public class MobTFTowerBoss extends MobTFTowerGhast {

	private static final int MAX_HEALTH = 240;

	private static final double SHOT_SPAWN_DISTANCE = 8.5;

	public static final String ENTITY_ID = TwilightForest.MOD_ID + ":urghast";

	private int spawnerX;
	private int spawnerY;
	private int spawnerZ;
	private boolean hasSpawnerHome;

	public MobTFTowerBoss(World world) {
		super(world);

		this.setSize(16.0F, 24.0F);
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected double shotSpawnDistance() {
		return SHOT_SPAWN_DISTANCE;
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	public void setSpawnerHome(int x, int y, int z) {
		this.spawnerX = x;
		this.spawnerY = y;
		this.spawnerZ = z;
		this.hasSpawnerHome = true;
	}

	@Override
	public void remove() {
		if (!this.world.isClientSide && this.hasSpawnerHome && this.getHealth() > 0) {

			this.hasSpawnerHome = false;

			this.world.setBlockWithNotify(this.spawnerX, this.spawnerY, this.spawnerZ,
				TFBlocks.BOSS_SPAWNER.id());
			if (this.world.getTileEntity(this.spawnerX, this.spawnerY, this.spawnerZ)
				instanceof TileEntityMobSpawner spawner) {
				spawner.setMobId(ENTITY_ID);
			}
		}

		super.remove();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("SpawnerX", this.spawnerX);
		tag.putInt("SpawnerY", this.spawnerY);
		tag.putInt("SpawnerZ", this.spawnerZ);
		tag.putBoolean("HasSpawner", this.hasSpawnerHome);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.spawnerX = tag.getInteger("SpawnerX");
		this.spawnerY = tag.getInteger("SpawnerY");
		this.spawnerZ = tag.getInteger("SpawnerZ");
		this.hasSpawnerHome = tag.getBoolean("HasSpawner");
	}
}
