package com.twilightforest.entity.ai;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class TFTaskPanic extends TFTask {

	private static final int MEMORY = 100;

	private static final int SCATTER_XZ = 5;
	private static final int SCATTER_Y = 4;

	private static final float PATH_SEARCH_RANGE = 16.0F;

	protected final TFBrainHost host;
	protected final TFBrain brain;
	private final float speed;

	private int alarmTicks;

	@Nullable
	private Entity attacker;

	private double targetX;
	private double targetY;
	private double targetZ;

	public TFTaskPanic(TFBrainHost host, TFBrain brain, float speed) {
		this.host = host;
		this.brain = brain;
		this.speed = speed;

		this.setMutexBits(MUTEX_MOVE);
	}

	public void alarm(@Nullable Entity attacker) {
		this.alarmTicks = MEMORY;
		this.attacker = attacker;
	}

	@Nullable
	public Entity getAttacker() {
		return this.alarmTicks > 0 ? this.attacker : null;
	}

	@Override
	public boolean shouldExecute() {
		MobPathfinder mob = this.host.asMob();
		if (this.alarmTicks <= 0 && !mob.isOnFire()) {
			return false;
		}

		Vector3d to = TFRandomPositionGenerator.findRandomTarget(this.host, SCATTER_XZ, SCATTER_Y);
		if (to == null) {
			return false;
		}
		this.targetX = to.x;
		this.targetY = to.y;
		this.targetZ = to.z;
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return !this.brain.noPath();
	}

	@Override
	public void startExecuting() {
		MobPathfinder mob = this.host.asMob();
		Path route = mob.world.getEntityPathToTilePos(mob,
			new TilePos(this.targetX, this.targetY, this.targetZ), PATH_SEARCH_RANGE);
		this.brain.setPath(route);
	}

	@Override
	public void resetTask() {
		this.brain.setPath(null);
	}

	@Override
	public void updateTask() {
		if (this.alarmTicks > 0 && --this.alarmTicks == 0) {
			this.attacker = null;
		}
	}

	@Override
	public float moveSpeed() {
		return this.speed;
	}

	@Override
	public VanillaAI vanillaAI() {
		return VanillaAI.NO_ROAM;
	}
}
