package com.twilightforest.entity.ai;

public abstract class TFTask {

	public enum VanillaAI {

		FULL,

		NO_ROAM,

		NONE;

		VanillaAI max(VanillaAI other) {
			return this.ordinal() >= other.ordinal() ? this : other;
		}
	}

	public static final float NO_SPEED = Float.NaN;

	private int mutexBits;

	protected final void setMutexBits(int bits) {
		this.mutexBits = bits;
	}

	public final int getMutexBits() {
		return this.mutexBits;
	}

	public static final int MUTEX_MOVE = 1;

	public static final int MUTEX_LOOK = 2;

	public static final int MUTEX_JUMP = 4;

	public abstract boolean shouldExecute();

	public boolean continueExecuting() {
		return this.shouldExecute();
	}

	public boolean isInterruptible() {
		return true;
	}

	public void startExecuting() {
	}

	public void resetTask() {
	}

	public void updateTask() {
	}

	public VanillaAI vanillaAI() {
		return VanillaAI.FULL;
	}

	public float moveSpeed() {
		return NO_SPEED;
	}

	public String name() {
		return this.getClass().getSimpleName();
	}
}
