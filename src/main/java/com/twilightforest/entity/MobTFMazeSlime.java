package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobSlime;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.weather.Weather;

public class MobTFMazeSlime extends MobSlime {

	private static final int HEALTH_MULTIPLIER = 2;

	private static final int DAMAGE_BONUS = 3;

	private static final float SOUND_VOLUME_PER_SIZE = 0.1F;

	private static final double TOUCH_REACH_PER_SIZE = 0.6;

	private static final int MAX_SPAWN_LIGHT = 4;

	private static final int SKYLIGHT_ROLL = 32;

	private static final int MAX_SPLIT_CHILDREN = 4;

	private static final float SPLIT_SCATTER = 4.0F;

	public MobTFMazeSlime(World world) {
		this(world, false);
	}

	private MobTFMazeSlime(World world, boolean split) {
		super(world, split);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "mazeslime");
	}

	@Override
	public void spawnInit() {

		this.setSlimeSize(1 << (1 + this.random.nextInt(2)));
	}

	@Override
	public int getMaxHealth() {
		int size = this.getSlimeSize();
		return HEALTH_MULTIPLIER * size * size;
	}

	public int getAttackStrength() {
		return this.getSlimeSize() + DAMAGE_BONUS;
	}

	@Override
	protected float getSoundVolume() {
		return SOUND_VOLUME_PER_SIZE * this.getSlimeSize();
	}

	@Override
	public void playerTouch(Player player) {
		int size = this.getSlimeSize();

		if (this.canEntityBeSeen(player)
			&& this.distanceTo(player) < TOUCH_REACH_PER_SIZE * size
			&& player.hurt(this, this.getAttackStrength(), DamageType.COMBAT)) {

			this.world.playSoundAtEntity(null, this, "mob.slimeattack", 1.0F,
				(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
		}
	}

	@Override
	public void remove() {
		int size = this.getSlimeSize();

		if (!this.world.isClientSide && size > 1 && this.getHealth() <= 0) {
			int children = this.world.rand.nextInt(MAX_SPLIT_CHILDREN) + 1;

			for (int i = 0; i < children; i++) {
				float offsetX = (i % 2 - 0.5F) * size / SPLIT_SCATTER;
				float offsetZ = (i / 2 - 0.5F) * size / SPLIT_SCATTER;

				MobTFMazeSlime child = new MobTFMazeSlime(this.world, true);
				child.setSlimeSize(size / 2);
				child.moveTo(this.x + offsetX, this.y + 0.5, this.z + offsetZ,
					this.random.nextFloat() * 360.0F, 0.0F);
				this.world.entityJoinedWorld(child);
			}
		}

		this.removed = true;
	}

	@Override
	public boolean canSpawnHere() {
		if (!this.world.getDifficulty().canHostileMobsSpawn()) {
			return false;
		}

		if (!this.world.checkIfAABBIsClear(this.bb)
			|| !this.world.getCubes(this, this.bb).isEmpty()
			|| this.world.getIsAnyLiquid(this.bb)) {
			return false;
		}

		return this.isValidLightLevel();
	}

	private boolean isValidLightLevel() {
		TilePos pos = new TilePos(this.x, this.bb.minY, this.z);

		if (this.world.getSavedLightValue(LightLayer.Block, pos) > 0) {
			return false;
		}
		if (this.world.getSavedLightValue(LightLayer.Sky, pos) > this.random.nextInt(SKYLIGHT_ROLL)) {
			return false;
		}

		int light = this.world.getBlockLightValue(pos);

		Weather weather = this.world.getCurrentWeather();
		if (weather != null && weather.isMobDaylightSpawnAllowed()) {
			light /= 2;
		}

		return light <= MAX_SPAWN_LIGHT;
	}
}
