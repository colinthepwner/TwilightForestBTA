package com.twilightforest.entity.ai.harness;

import com.twilightforest.entity.ai.TFBrainHost;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.world.pos.TilePosc;

import java.util.function.ToDoubleFunction;

public final class TestHost implements TFBrainHost {

	public final MobPathfinder mob;

	public float speed = Float.NaN;

	public boolean randomWalk = true;

	public boolean driven;
	public float drivenYRot;
	public float drivenForward;
	public boolean drivenJumping;

	public int vanillaRuns;

	public ToDoubleFunction<TilePosc> weight = pos -> 0.0;

	public TestHost(MobPathfinder mob) {
		this.mob = mob;
	}

	public Runnable vanillaAI() {
		return () -> this.vanillaRuns++;
	}

	@Override
	public MobPathfinder asMob() {
		return this.mob;
	}

	@Override
	public float tfBlockPathWeight(TilePosc pos) {
		return (float) this.weight.applyAsDouble(pos);
	}

	@Override
	public void tfSetSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public void tfSetRandomWalk(boolean enabled) {
		this.randomWalk = enabled;
	}

	@Override
	public void tfDrive(float yRot, float moveForward, boolean jumping) {
		this.driven = true;
		this.drivenYRot = yRot;
		this.drivenForward = moveForward;
		this.drivenJumping = jumping;
	}
}
