package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.ai.TFPathfinder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.joml.primitives.AABBd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobTFKobold extends MobMonster {

	private static final int DATA_PANICKED = 17;

	private static final double FLOCK_RANGE_XZ = 16.0;
	private static final double FLOCK_RANGE_Y = 4.0;

	private static final double FLOCK_MIN_SQ = 25.0;

	private static final double FLOCK_MAX_SQ = 256.0;

	private static final int FLOCK_ROLL = 40;

	private static final int FLOCK_REPATH = 10;

	private static final double PANIC_RANGE_XZ = 4.0;
	private static final double PANIC_RANGE_Y = 2.0;

	private static final int PANIC_TICKS = 40;

	private static final int PANIC_RESET = 20;

	private static final float PANIC_SPEED = 1.15F;
	private static final float NORMAL_SPEED = 0.85F;

	private static final int PACK_MINIMUM = 4;

	private static final double PACK_LOOK = 16.0;

	private static final int PACK_SPREAD = 3;

	private static final int SCATTER_XZ = 5;
	private static final int SCATTER_Y = 4;

	private static final int SCATTER_TRIES = 10;

	private static final int AVOID_XZ = 2;
	private static final int AVOID_Y = 1;

	private boolean flocking;
	private double flockX;
	private double flockY;
	private double flockZ;
	private int flockMoveTimer;

	private Path flockPath;

	private int fleeTimer;

	private boolean broughtIn;

	public MobTFKobold(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "kobold");
		this.setSize(0.8F, 1.1F);
		this.moveSpeed = NORMAL_SPEED;

		this.attackStrength = 3;
	}

	@Override
	public int getMaxHealth() {
		return 13;
	}

	@Override
	public void spawnInit() {
		super.spawnInit();

		if (this.world.isClientSide || this.broughtIn) {
			return;
		}

		AABBd box = MathHelper.aabbGrow(this.bb, PACK_LOOK, PACK_LOOK, PACK_LOOK, new AABBd());
		int already = this.world.getEntitiesWithinAABB(MobTFKobold.class, box).size();

		for (int i = already; i < PACK_MINIMUM; i++) {
			MobTFKobold mate = new MobTFKobold(this.world);
			mate.broughtIn = true;
			mate.moveTo(
				this.x + this.random.nextInt(PACK_SPREAD * 2 + 1) - PACK_SPREAD,
				this.y,
				this.z + this.random.nextInt(PACK_SPREAD * 2 + 1) - PACK_SPREAD,
				this.random.nextFloat() * 360.0F, 0.0F);

			if (mate.canSpawnHere()) {
				this.world.entityJoinedWorld(mate);
			}
		}
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_PANICKED, (byte) 0, Byte.class);
	}

	public boolean isPanicked() {
		return this.entityData.getByte(DATA_PANICKED) != 0;
	}

	private void setPanicked(boolean panicked) {
		this.entityData.set(DATA_PANICKED, (byte) (panicked ? 127 : 0));
	}

	@Override
	protected void updateAI() {
		if (this.tickPanic()) {
			return;
		}
		super.updateAI();
		this.tickFlock();
	}

	private boolean tickPanic() {
		if (this.isPanicked()) {
			this.fleeTimer--;

			if (this.fleeTimer > 0 && this.hasPath()) {
				this.steerAlongPath();
				return true;
			}

			this.fleeTimer = Math.max(0, this.fleeTimer - PANIC_RESET);
			this.stopPanicking();
		}

		Entity dying = this.findDyingFlockmate();
		if (dying == null && this.fleeTimer <= 0) {
			return false;
		}
		return this.startPanicking(dying);
	}

	private Entity findDyingFlockmate() {
		AABBd box = MathHelper.aabbGrow(this.bb, PANIC_RANGE_XZ, PANIC_RANGE_Y, PANIC_RANGE_XZ,
			new AABBd());

		for (MobTFKobold flocker : this.world.getEntitiesWithinAABB(MobTFKobold.class, box)) {
			if (flocker.deathTime > 0) {
				return flocker;
			}
		}
		return null;
	}

	private boolean startPanicking(Entity dying) {
		int bestX = 0;
		int bestY = 0;
		int bestZ = 0;
		float bestWeight = -99999.0F;
		boolean found = false;

		for (int i = 0; i < SCATTER_TRIES; i++) {
			int cx = MathHelper.floor(this.x + this.random.nextInt(SCATTER_XZ * 2 + 1) - SCATTER_XZ);
			int cy = MathHelper.floor(this.y + this.random.nextInt(SCATTER_Y * 2 + 1) - SCATTER_Y);
			int cz = MathHelper.floor(this.z + this.random.nextInt(SCATTER_XZ * 2 + 1) - SCATTER_XZ);

			float weight = this.getBlockPathWeight(new TilePos(cx, cy, cz));
			if (weight > bestWeight) {
				bestWeight = weight;
				bestX = cx;
				bestY = cy;
				bestZ = cz;
				found = true;
			}
		}

		if (!found) {
			return false;
		}

		Path route = TFPathfinder.findPath(this.world, this, bestX, bestY, bestZ, 1.0,
			deathSite(dying));
		if (route == null) {
			return false;
		}

		this.pathToEntity = route;
		this.fleeTimer = PANIC_TICKS;
		this.setPanicked(true);
		this.moveSpeed = PANIC_SPEED;

		this.target = null;
		this.steerAlongPath();
		return true;
	}

	private Set<Long> deathSite(Entity dying) {
		if (dying == null) {
			return null;
		}

		int dx = MathHelper.floor(dying.x);
		int dy = MathHelper.floor(dying.y);
		int dz = MathHelper.floor(dying.z);

		Set<Long> cells = new HashSet<>();
		for (int x = -AVOID_XZ; x <= AVOID_XZ; x++) {
			for (int y = -AVOID_Y; y <= AVOID_Y; y++) {
				for (int z = -AVOID_XZ; z <= AVOID_XZ; z++) {
					cells.add(TFPathfinder.packKey(dx + x, dy + y, dz + z));
				}
			}
		}
		return cells;
	}

	private void stopPanicking() {
		this.setPanicked(false);
		this.moveSpeed = NORMAL_SPEED;
	}

	private void tickFlock() {
		if (this.isPanicked() || this.getTarget() != null) {
			this.stopFlocking();
			return;
		}

		if (!this.flocking) {
			if (this.random.nextInt(FLOCK_ROLL) != 0) {
				return;
			}
			if (!this.findFlockCentre()) {
				return;
			}
			this.flocking = true;
			this.flockMoveTimer = 0;
		}

		double distSq = this.distanceToSqr(this.flockX, this.flockY, this.flockZ);
		if (distSq < FLOCK_MIN_SQ || distSq > FLOCK_MAX_SQ) {
			this.stopFlocking();
			return;
		}

		if (--this.flockMoveTimer <= 0 || (this.flockPath != null && this.flockPath.isDone())) {
			this.flockMoveTimer = FLOCK_REPATH;
			this.flockPath = this.world.getEntityPathToXYZ(this,
				(int) this.flockX, (int) this.flockY, (int) this.flockZ, 16.0F);
		}

		if (this.flockPath == null) {
			this.stopFlocking();
			return;
		}

		this.pathToEntity = this.flockPath;
		this.steerAlongPath();

		this.flockPath = this.pathToEntity;
		if (this.flockPath == null) {
			this.flocking = false;
		}
	}

	private void stopFlocking() {
		this.flocking = false;
		this.flockPath = null;
	}

	private boolean findFlockCentre() {
		AABBd box = MathHelper.aabbGrow(this.bb, FLOCK_RANGE_XZ, FLOCK_RANGE_Y, FLOCK_RANGE_XZ,
			new AABBd());

		List<MobTFKobold> flock = this.world.getEntitiesWithinAABB(MobTFKobold.class, box);
		if (flock.isEmpty()) {
			return false;
		}

		double sumX = 0.0;
		double sumY = 0.0;
		double sumZ = 0.0;
		for (MobTFKobold flocker : flock) {
			sumX += flocker.x;
			sumY += flocker.y;
			sumZ += flocker.z;
		}

		double cx = sumX / flock.size();
		double cy = sumY / flock.size();
		double cz = sumZ / flock.size();

		if (this.distanceToSqr(cx, cy, cz) < FLOCK_MIN_SQ) {
			return false;
		}

		this.flockX = cx;
		this.flockY = cy;
		this.flockZ = cz;
		return true;
	}

	private void steerAlongPath() {

		if (this.pathToEntity == null || this.pathToEntity.isDone()) {
			this.pathToEntity = null;
			return;
		}

		org.joml.Vector3dc next = this.pathToEntity.getPos(this);
		double reach = this.bbWidth * 2.0F;

		while (next != null && next.distanceSquared(this.x, next.y(), this.z) < reach * reach) {
			this.pathToEntity.next();
			if (this.pathToEntity.isDone()) {
				this.pathToEntity = null;
				next = null;
			} else {
				next = this.pathToEntity.getPos(this);
			}
		}

		this.isJumping = false;
		if (next == null) {
			return;
		}

		int floor = MathHelper.floor(this.bb.minY + 0.5);
		double dx = next.x() - this.x;
		double dz = next.z() - this.z;
		double dy = next.y() - floor;

		float turn = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F - this.yRot;
		this.moveForward = this.moveSpeed;

		while (turn < -180.0F) {
			turn += 360.0F;
		}
		while (turn >= 180.0F) {
			turn -= 360.0F;
		}
		if (turn > 30.0F) {
			turn = 30.0F;
		}
		if (turn < -30.0F) {
			turn = -30.0F;
		}

		this.yRot += turn;
		if (dy > 0.0) {
			this.isJumping = true;
		}
	}

	@Override
	public void onLivingUpdate() {
		if (this.isPanicked()) {
			for (int i = 0; i < 2; i++) {
				this.world.spawnParticle("splash",
					this.x + (this.random.nextDouble() - 0.5) * this.bbWidth * 0.5,
					this.y + this.getHeadHeight(),
					this.z + (this.random.nextDouble() - 0.5) * this.bbWidth * 0.5,
					0.0, 0.0, 0.0, 0, false);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.kobold";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.die";
	}
}
