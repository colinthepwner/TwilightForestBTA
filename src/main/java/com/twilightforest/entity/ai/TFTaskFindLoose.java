package com.twilightforest.entity.ai;

import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;

import java.util.List;

public class TFTaskFindLoose extends TFTask {

	private static final double SEARCH_XZ = 16.0;
	private static final double SEARCH_Y = 4.0;

	static final double REACH_SQ = 6.25;

	private static final int COOLDOWN = 100;

	private static final float PATH_SEARCH_RANGE = 16.0F;

	private static final float MAX_YAW_TURN = 30.0F;

	private static final float MAX_PITCH_TURN = 40.0F;

	protected final TFBrainHost host;
	protected final TFBrain brain;
	private final float pursueSpeed;

	private final int temptId;

	private int cooldown;

	@Nullable
	private EntityItem temptingItem;

	public TFTaskFindLoose(TFBrainHost host, TFBrain brain, float pursueSpeed, int temptId) {
		this.host = host;
		this.brain = brain;
		this.pursueSpeed = pursueSpeed;
		this.temptId = temptId;

		this.setMutexBits(MUTEX_MOVE | MUTEX_LOOK);
	}

	@Override
	public boolean shouldExecute() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}

		this.temptingItem = null;

		MobPathfinder mob = this.host.asMob();
		AABBd box = MathHelper.aabbGrow(mob.bb, SEARCH_XZ, SEARCH_Y, SEARCH_XZ, new AABBd());
		List<EntityItem> nearby = mob.world.getEntitiesWithinAABB(EntityItem.class, box);

		for (EntityItem item : nearby) {

			if (item.item != null && item.item.itemID == this.temptId && item.isAlive()) {
				this.temptingItem = item;
				break;
			}
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

		MobPathfinder mob = this.host.asMob();

		mob.lookAt(item, MAX_YAW_TURN, MAX_PITCH_TURN);

		if (mob.distanceToSqr(item) < REACH_SQ) {

			this.brain.setPath(null);
			return;
		}

		Path route = mob.world.getEntityPathToTilePos(mob,
			new TilePos(item.x, item.y, item.z), PATH_SEARCH_RANGE);
		this.brain.setPath(route);
	}

	@Override
	public float moveSpeed() {
		return this.pursueSpeed;
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
