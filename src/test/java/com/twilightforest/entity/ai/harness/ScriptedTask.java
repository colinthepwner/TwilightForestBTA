package com.twilightforest.entity.ai.harness;

import com.twilightforest.entity.ai.TFTask;

import java.util.ArrayList;
import java.util.List;

public final class ScriptedTask extends TFTask {

	public final List<String> log = new ArrayList<>();
	private final String label;

	public boolean should = true;
	public Boolean canContinue;
	public boolean interruptible = true;
	public VanillaAI vanilla = VanillaAI.FULL;
	public float speed = NO_SPEED;

	public ScriptedTask(String label, int mutexBits) {
		this.label = label;
		this.setMutexBits(mutexBits);
	}

	@Override
	public boolean shouldExecute() {
		this.log.add("should=" + this.should);
		return this.should;
	}

	@Override
	public boolean continueExecuting() {
		boolean answer = this.canContinue == null ? this.should : this.canContinue;
		this.log.add("continue=" + answer);
		return answer;
	}

	@Override
	public boolean isInterruptible() {
		return this.interruptible;
	}

	@Override
	public void startExecuting() {
		this.log.add("start");
	}

	@Override
	public void resetTask() {
		this.log.add("reset");
	}

	@Override
	public void updateTask() {
		this.log.add("update");
	}

	@Override
	public VanillaAI vanillaAI() {
		return this.vanilla;
	}

	@Override
	public float moveSpeed() {
		return this.speed;
	}

	@Override
	public String name() {
		return this.label;
	}

	public List<String> events() {
		List<String> out = new ArrayList<>();
		for (String s : this.log) {
			if (!s.startsWith("should=") && !s.startsWith("continue=")) {
				out.add(s);
			}
		}
		return out;
	}

	public void clear() {
		this.log.clear();
	}
}
