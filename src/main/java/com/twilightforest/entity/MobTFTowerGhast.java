package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobGhast;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.projectile.ProjectileFireball;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;

public class MobTFTowerGhast extends MobGhast {

	public static final byte STATE_CALM = 0;

	public static final byte STATE_STARING = 1;

	public static final byte STATE_FIRING = 2;

	protected static final int DATA_AGGRO_STATE = 17;

	private static final int MAX_HEALTH = 30;

	private static final float SOUND_VOLUME = 0.5F;

	private static final int AMBIENT_INTERVAL = 160;

	private static final int MAX_PER_CHUNK = 8;

	private static final float VERTICAL_FACE_SPEED = 500.0F;

	private static final float HORIZONTAL_FACE_SPEED = 10.0F;

	private static final int AGGRO_TICKS = 20;

	private static final int CHARGE_SOUND_AT = 10;
	private static final int FIRE_AT = 20;
	private static final int RELOAD = -40;

	private static final int CALM_DOWN_ONE_IN = 6;

	private static final int CLIENT_CHARGE_CEILING = 19;

	private static final double HOME_CORRECTION_STEP = 16.0;

	private static final int DAYLIGHT_AGE_PENALTY = 2;

	private static final int HOMELESS_AGE_PENALTY = 5;

	private static final int HOME_RADIUS = 64;

	private static final int HOME_HEIGHT = TFWorldConstants.WORLD_HEIGHT;

	private static final int HOME_MIN_Y = TFWorldConstants.SEA_LEVEL * 2;

	@Nullable
	protected Entity targetedEntity;

	protected boolean isAggressive;

	protected int aggroCounter;

	protected float aggroRange = 64.0F;

	protected float stareRange = 32.0F;

	protected float wanderFactor = 16.0F;

	private int homeX;
	private int homeY;
	private int homeZ;
	private float homeRadius = -1.0F;

	public MobTFTowerGhast(World world) {
		super(world);

		this.setTextureIdentifier(TwilightForest.MOD_ID, "towerghast");
		this.setSize(4.0F, 6.0F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_AGGRO_STATE, (byte) STATE_CALM, Byte.class);
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected float getSoundVolume() {
		return SOUND_VOLUME;
	}

	@Override
	public int getAmbientSoundInterval() {
		return AMBIENT_INTERVAL;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return MAX_PER_CHUNK;
	}

	public byte getAggroState() {
		return this.entityData.getByte(DATA_AGGRO_STATE);
	}

	@Override
	@NotNull
	public String getEntityTexture() {
		byte state = this.getAggroState();
		if (state != STATE_STARING && state != STATE_FIRING) {
			return this.basePath + "0.png";
		}
		return this.basePath + state + ".png";
	}

	@Override
	@NotNull
	public String getDefaultEntityTexture() {
		return this.getEntityTexture();
	}

	@Override
	public void onLivingUpdate() {
		this.attackChargeO = this.attackCharge;

		if (this.world.isClientSide) {
			this.attackCharge += this.getAggroState() == STATE_FIRING ? 1 : -1;
			if (this.attackCharge < 0) {
				this.attackCharge = 0;
			} else if (this.attackCharge > CLIENT_CHARGE_CEILING) {
				this.attackCharge = CLIENT_CHARGE_CEILING;
			}
		}

		if (this.calcBrightness(1.0F) > 0.5F) {
			this.entityAge += DAYLIGHT_AGE_PENALTY;
		}

		if (this.random.nextBoolean()) {
			this.world.spawnParticle("reddust",
				this.x + (this.random.nextDouble() - 0.5) * this.bbWidth,
				this.y + this.random.nextDouble() * this.bbHeight - 0.25,
				this.z + (this.random.nextDouble() - 0.5) * this.bbWidth,
				0.0, 0.0, 0.0, 0, false);
		}

		super.onLivingUpdate();
	}

	@Override
	protected void updateAI() {

		if (!this.world.isClientSide && !this.world.getDifficulty().canHostileMobsSpawn()) {
			this.remove();
			return;
		}
		this.tryToDespawn();
		this.checkForTowerHome();

		if (this.targetedEntity != null && !this.targetedEntity.isAlive()) {
			this.targetedEntity = null;
		}
		if (this.targetedEntity == null) {
			this.targetedEntity = this.findPlayerInRange();
		} else if (!this.isAggressive && this.targetedEntity instanceof Player) {
			this.checkToIncreaseAggro((Player) this.targetedEntity);
		}

		double offsetX = this.waypointX - this.x;
		double offsetY = this.waypointY - this.y;
		double offsetZ = this.waypointZ - this.z;
		double distanceDesired = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

		if (distanceDesired < 1.0 || distanceDesired > 3600.0) {
			this.waypointX = this.x + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
			this.waypointY = this.y + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
			this.waypointZ = this.z + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
		}

		if (this.targetedEntity == null) {

			if (this.courseChangeCooldown-- <= 0) {
				this.courseChangeCooldown += this.random.nextInt(20) + 20;
				distanceDesired = Math.sqrt(distanceDesired);

				if (!this.isWithinHomeDistance(MathHelper.floor(this.waypointX),
					MathHelper.floor(this.waypointY), MathHelper.floor(this.waypointZ))) {

					int[] centre = TFFeature.nearestFeatureCenter(this.world,
						MathHelper.floor(this.x) >> 4, MathHelper.floor(this.z) >> 4);
					double towerX = ((MathHelper.floor(this.x) >> 4) << 4) + centre[0];
					double towerZ = ((MathHelper.floor(this.z) >> 4) << 4) + centre[1];

					double homeVecX = towerX - this.x;
					double homeVecY = HOME_HEIGHT - this.y;
					double homeVecZ = towerZ - this.z;
					double length = Math.sqrt(homeVecX * homeVecX + homeVecY * homeVecY
						+ homeVecZ * homeVecZ);
					if (length > 0.0) {
						homeVecX /= length;
						homeVecY /= length;
						homeVecZ /= length;
					}

					this.waypointX = this.x + homeVecX * HOME_CORRECTION_STEP
						+ (this.random.nextFloat() * 2.0F - 1.0F) * HOME_CORRECTION_STEP;
					this.waypointY = this.y + homeVecY * HOME_CORRECTION_STEP
						+ (this.random.nextFloat() * 2.0F - 1.0F) * HOME_CORRECTION_STEP;
					this.waypointZ = this.z + homeVecZ * HOME_CORRECTION_STEP
						+ (this.random.nextFloat() * 2.0F - 1.0F) * HOME_CORRECTION_STEP;
				}

				if (this.isCourseTraversable(distanceDesired)) {
					this.xd += offsetX / distanceDesired * 0.1;
					this.yd += offsetY / distanceDesired * 0.1;
					this.zd += offsetZ / distanceDesired * 0.1;
				} else {
					this.waypointX = this.x;
					this.waypointY = this.y;
					this.waypointZ = this.z;
				}
			}
		} else {

			this.xd = 0.0;
			this.yd = 0.0;
			this.zd = 0.0;
		}

		double targetRange = this.aggroCounter > 0 || this.isAggressive
			? this.aggroRange : this.stareRange;

		if (this.targetedEntity != null
			&& this.targetedEntity.distanceToSqr(this) < targetRange * targetRange
			&& this.canEntityBeSeen(this.targetedEntity)) {

			this.lookAt(this.targetedEntity, HORIZONTAL_FACE_SPEED, VERTICAL_FACE_SPEED);

			if (this.isAggressive) {
				if (this.attackCharge == CHARGE_SOUND_AT) {
					this.world.playSoundAtEntity(null, this, "mob.ghast.charge", this.getSoundVolume(),
						(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}
				this.attackCharge++;
				if (this.attackCharge == FIRE_AT) {
					this.spitFireball();
					this.attackCharge = RELOAD;
				}
			}
		} else {
			this.isAggressive = false;
			this.targetedEntity = null;

			this.yBodyRot = this.yRot = -((float) Math.atan2(this.xd, this.zd)) * 180.0F / (float) Math.PI;
			this.xRot = 0.0F;
		}

		if (this.attackCharge > 0 && !this.isAggressive) {
			this.attackCharge--;
		}

		byte newState = aggroStateFor(this.attackCharge,
			this.aggroCounter > 0 || this.isAggressive);
		if (this.getAggroState() != newState) {
			this.entityData.set(DATA_AGGRO_STATE, newState);
		}
	}

	public static byte aggroStateFor(int attackCharge, boolean staring) {
		if (attackCharge > CHARGE_SOUND_AT) {
			return STATE_FIRING;
		}
		return staring ? STATE_STARING : STATE_CALM;
	}

	protected double shotSpawnDistance() {
		return 0.5;
	}

	protected void spitFireball() {
		if (this.targetedEntity == null) {
			return;
		}

		double offsetX = this.targetedEntity.x - this.x;
		double offsetY = this.targetedEntity.bb.minY + this.targetedEntity.bbHeight / 2.0F
			- (this.y + this.bbHeight / 2.0F);
		double offsetZ = this.targetedEntity.z - this.z;

		this.world.playSoundAtEntity(null, this, "mob.ghast.fireball", this.getSoundVolume(),
			(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

		ProjectileFireball fireball = new ProjectileFireball(this.world, this, offsetX, offsetY, offsetZ);
		double distance = this.shotSpawnDistance();
		Vector3dc look = this.getViewVector(1.0F);

		fireball.setPos(
			this.x + look.x() * distance,
			this.y + this.bbHeight / 2.0F + look.y() * distance,
			this.z + look.z() * distance);
		this.world.entityJoinedWorld(fireball);

		if (this.random.nextInt(CALM_DOWN_ONE_IN) == 0) {
			this.isAggressive = false;
		}
	}

	@Nullable
	protected Entity findPlayerInRange() {
		Player closest = this.world.getClosestPlayerToEntity(this, this.aggroRange);
		if (closest != null
			&& (this.distanceTo(closest) < this.stareRange || this.shouldAttackPlayer(closest))) {
			return closest;
		}
		return null;
	}

	protected void checkToIncreaseAggro(Player player) {
		if (this.shouldAttackPlayer(player)) {
			if (this.aggroCounter == 0) {
				this.world.playSoundAtEntity(null, this, "mob.ghast.moan", 1.0F, 1.0F);
			}
			if (this.aggroCounter++ >= AGGRO_TICKS) {
				this.aggroCounter = 0;
				this.isAggressive = true;
			}
		} else {
			this.aggroCounter = 0;
		}
	}

	protected boolean shouldAttackPlayer(Player player) {
		return this.world.canBlockSeeTheSky(
			MathHelper.floor(player.x), MathHelper.floor(player.y), MathHelper.floor(player.z))
			&& player.canEntityBeSeen(this);
	}

	protected boolean isCourseTraversable(double steps) {
		if (steps <= 0.0) {
			return false;
		}
		double stepX = (this.waypointX - this.x) / steps;
		double stepY = (this.waypointY - this.y) / steps;
		double stepZ = (this.waypointZ - this.z) / steps;

		AABBd swept = new AABBd(this.bb);
		for (int i = 1; i < steps; i++) {
			swept.translate(stepX, stepY, stepZ);
			if (!this.world.areBlocksLoaded(swept) || !this.world.getCubes(this, swept).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		boolean wasAttacked = super.hurt(attacker, damage, type);
		if (wasAttacked && attacker != null && attacker != this) {
			this.targetedEntity = attacker;
			this.isAggressive = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean canSpawnHere() {
		return this.world.areBlocksLoaded(this.bb)
			&& this.world.getCubes(this, this.bb).isEmpty()
			&& !this.world.getIsAnyLiquid(this.bb)
			&& this.world.getDifficulty().canHostileMobsSpawn()
			&& this.isValidLightLevel();
	}

	protected boolean isValidLightLevel() {
		return true;
	}

	protected void checkForTowerHome() {
		if (this.homeRadius >= 0.0F) {
			return;
		}

		int chunkX = MathHelper.floor(this.x) >> 4;
		int chunkZ = MathHelper.floor(this.z) >> 4;

		if (TFFeature.nearestFeatureType(this.world, chunkX, chunkZ) != TFFeature.DARK_TOWER) {
			this.entityAge += HOMELESS_AGE_PENALTY;
			return;
		}

		int[] centre = TFFeature.nearestFeatureCenter(this.world, chunkX, chunkZ);
		this.homeX = (chunkX << 4) + centre[0];
		this.homeY = HOME_HEIGHT;
		this.homeZ = (chunkZ << 4) + centre[1];
		this.homeRadius = HOME_RADIUS;
	}

	public boolean isWithinHomeDistance(int x, int y, int z) {
		return withinHomeCylinder(this.homeX, this.homeZ, this.homeRadius, x, y, z);
	}

	public static boolean withinHomeCylinder(int homeX, int homeZ, float radius, int x, int y, int z) {
		if (radius < 0.0F) {
			return true;
		}
		if (y <= HOME_MIN_Y || y >= TFWorldConstants.WORLD_HEIGHT) {
			return false;
		}
		double dx = x - homeX;
		double dz = z - homeZ;
		return dx * dx + dz * dz < radius * radius;
	}

	public float getHomeRadius() {
		return this.homeRadius;
	}
}
