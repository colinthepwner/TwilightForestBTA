package com.twilightforest.entity.ai;

import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;

public class TFTaskEatLoose extends TFTask {

	public interface WoolEater extends TFBrainHost {

		boolean isColorPresent(int color);

		void setColorPresent(int color);

		void animateAddColor(int color, int iterations);

		void bleat();
	}

	private static final double SEARCH_RADIUS = 2.0;

	private static final int EAT_PARTICLES = 50;

	private static final int COOLDOWN = 100;

	private static final float MAX_YAW_TURN = 30.0F;
	private static final float MAX_PITCH_TURN = 40.0F;

	protected final WoolEater ram;
	protected final TFBrain brain;

	private final int temptId;

	private int cooldown;

	@Nullable
	private EntityItem temptingItem;

	public TFTaskEatLoose(WoolEater ram, TFBrain brain, int temptId) {
		this.ram = ram;
		this.brain = brain;
		this.temptId = temptId;

		this.setMutexBits(0);
	}

	@Override
	public boolean shouldExecute() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}

		this.temptingItem = null;

		MobPathfinder mob = this.ram.asMob();
		AABBd box = MathHelper.aabbGrow(mob.bb, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS, new AABBd());
		List<EntityItem> nearby = mob.world.getEntitiesWithinAABB(EntityItem.class, box);

		for (EntityItem item : nearby) {
			if (item.item == null || item.item.itemID != this.temptId || !item.isAlive()) {
				continue;
			}
			if (this.ram.isColorPresent(item.item.getMetadata())) {
				continue;
			}
			this.temptingItem = item;
			break;
		}

		return this.temptingItem != null;
	}

	@Override
	public boolean continueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void resetTask() {
		this.temptingItem = null;
		this.brain.setPath(null);
		this.cooldown = COOLDOWN;
	}

	@Override
	public void updateTask() {
		EntityItem item = this.temptingItem;
		if (item == null) {
			return;
		}

		MobPathfinder mob = this.ram.asMob();
		mob.lookAt(item, MAX_YAW_TURN, MAX_PITCH_TURN);

		int color = item.item.getMetadata();

		if (mob.distanceToSqr(item) >= TFTaskFindLoose.REACH_SQ || this.ram.isColorPresent(color)) {
			return;
		}

		item.remove();

		this.ram.bleat();
		this.ram.setColorPresent(color);
		this.ram.animateAddColor(color, EAT_PARTICLES);
	}

	@Override
	public VanillaAI vanillaAI() {
		return VanillaAI.NO_ROAM;
	}

	@Nullable
	public EntityItem getTemptingItem() {
		return this.temptingItem;
	}
}
