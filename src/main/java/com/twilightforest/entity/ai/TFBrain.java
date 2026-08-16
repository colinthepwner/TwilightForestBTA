package com.twilightforest.entity.ai;

import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;

public final class TFBrain {

	public static final int SELECT_INTERVAL = 3;

	private static final float MAX_TURN = 30.0F;

	private final TFBrainHost host;
	private final MobPathfinder mob;

	private final float baseSpeed;

	private final List<Entry> entries = new ArrayList<>();

	private int tickCount;

	@Nullable
	private Path path;

	public TFBrain(TFBrainHost host, float baseSpeed) {
		this.host = host;
		this.mob = host.asMob();
		this.baseSpeed = baseSpeed;
	}

	public TFBrain add(int priority, TFTask task) {
		this.entries.add(new Entry(priority, task));

		this.entries.sort((a, b) -> Integer.compare(a.priority, b.priority));
		return this;
	}

	public void tick(Runnable vanillaAI) {
		this.select();

		TFTask.VanillaAI mode = TFTask.VanillaAI.FULL;
		float speed = this.baseSpeed;
		boolean speedClaimed = false;
		for (Entry e : this.entries) {
			if (!e.running) {
				continue;
			}
			mode = mode.max(e.task.vanillaAI());

			if (!speedClaimed && !Float.isNaN(e.task.moveSpeed())) {
				speed = e.task.moveSpeed();
				speedClaimed = true;
			}
		}
		this.host.tfSetSpeed(speed);

		if (this.path != null && this.path.isDone()) {
			this.path = null;
		}

		if (mode != TFTask.VanillaAI.NONE) {
			this.host.tfSetRandomWalk(mode == TFTask.VanillaAI.FULL);
			vanillaAI.run();

			if (this.path != null && !this.path.isDone() && this.mob.getTarget() == null) {
				this.mob.setPathToEntity(this.path);
			}
		}

		for (Entry e : this.entries) {
			if (e.running) {
				e.task.updateTask();
			}
		}

		if (mode == TFTask.VanillaAI.NONE) {
			this.steerAlongPath();
		}
	}

	private void select() {
		boolean fullPass = this.tickCount++ % SELECT_INTERVAL == 0;

		for (Entry e : this.entries) {
			if (e.running) {

				if (!e.task.continueExecuting() || (fullPass && !this.canUse(e))) {
					e.running = false;
					e.task.resetTask();
				}
				continue;
			}
			if (fullPass && this.canUse(e) && e.task.shouldExecute()) {
				e.running = true;
				e.task.startExecuting();
			}
		}
	}

	private boolean canUse(Entry candidate) {
		for (Entry other : this.entries) {
			if (other == candidate || !other.running) {
				continue;
			}
			if (candidate.priority >= other.priority) {
				if ((candidate.task.getMutexBits() & other.task.getMutexBits()) != 0) {
					return false;
				}
			} else if (!other.task.isInterruptible()) {
				return false;
			}
		}
		return true;
	}

	public void setPath(@Nullable Path path) {
		this.path = path;
		this.mob.setPathToEntity(path);
	}

	public boolean noPath() {
		return this.path == null || this.path.isDone();
	}

	@Nullable
	public Path getPath() {
		return this.path;
	}

	public boolean isRunning(TFTask task) {
		for (Entry e : this.entries) {
			if (e.task == task) {
				return e.running;
			}
		}
		return false;
	}

	private void steerAlongPath() {
		if (this.path == null || this.path.isDone()) {
			this.setPath(null);

			this.host.tfDrive(this.mob.yRot, 0.0F, false);
			return;
		}

		Vector3dc next = this.path.getPos(this.mob);
		double reach = this.mob.bbWidth * 2.0F;

		while (next != null && next.distanceSquared(this.mob.x, next.y(), this.mob.z) < reach * reach) {
			this.path.next();
			if (this.path.isDone()) {
				this.setPath(null);
				next = null;
			} else {
				next = this.path.getPos(this.mob);
			}
		}

		if (next == null) {
			this.host.tfDrive(this.mob.yRot, 0.0F, false);
			return;
		}

		int floor = MathHelper.floor(this.mob.bb.minY + 0.5);
		double dx = next.x() - this.mob.x;
		double dz = next.z() - this.mob.z;
		double dy = next.y() - floor;

		float turn = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F - this.mob.yRot;
		while (turn < -180.0F) {
			turn += 360.0F;
		}
		while (turn >= 180.0F) {
			turn -= 360.0F;
		}
		turn = Math.max(-MAX_TURN, Math.min(MAX_TURN, turn));

		this.host.tfDrive(this.mob.yRot + turn, this.currentSpeed(), dy > 0.0);
	}

	private float currentSpeed() {
		for (Entry e : this.entries) {
			if (e.running && !Float.isNaN(e.task.moveSpeed())) {
				return e.task.moveSpeed();
			}
		}
		return this.baseSpeed;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TFBrain[");
		for (Entry e : this.entries) {
			if (e.running) {
				sb.append(e.priority).append(':').append(e.task.name()).append(' ');
			}
		}
		return sb.append(']').toString();
	}

	private static final class Entry {
		final int priority;
		final TFTask task;
		boolean running;

		Entry(int priority, TFTask task) {
			this.priority = priority;
			this.task = task;
		}
	}
}
