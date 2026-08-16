package com.twilightforest.entity.ai;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pathfinder.Node;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.primitives.AABBd;

import java.util.List;

public class TFTaskAvoidEntity extends TFTask {

	private static final int ESCAPE_XZ = 16;
	private static final int ESCAPE_Y = 7;

	private static final double SPRINT_RANGE_SQ = 49.0;

	private static final double SEARCH_HEIGHT = 3.0;

	private static final float PATH_SEARCH_RANGE = 16.0F;

	private static final int DESTINATION_SLACK = 1;

	protected final TFBrainHost host;
	protected final TFBrain brain;
	private final Class<? extends Entity> threatClass;
	private final double triggerRange;
	private final float farSpeed;
	private final float nearSpeed;

	@Nullable
	private Entity threat;

	@Nullable
	private Path plannedRoute;

	public TFTaskAvoidEntity(TFBrainHost host, TFBrain brain, Class<? extends Entity> threatClass,
	                         double triggerRange, float farSpeed, float nearSpeed) {
		this.host = host;
		this.brain = brain;
		this.threatClass = threatClass;
		this.triggerRange = triggerRange;
		this.farSpeed = farSpeed;
		this.nearSpeed = nearSpeed;
		this.setMutexBits(MUTEX_MOVE);
	}

	@Override
	public boolean shouldExecute() {
		MobPathfinder mob = this.host.asMob();

		if (mob.getTarget() != null) {
			return false;
		}

		Entity found = this.findThreat(mob);
		if (found == null) {
			return false;
		}

		if (!mob.canEntityBeSeen(found)) {
			return false;
		}

		Vector3d escape = TFRandomPositionGenerator.findRandomTargetAwayFrom(this.host,
			ESCAPE_XZ, ESCAPE_Y, TFRandomPositionGenerator.at(found));
		if (escape == null) {

			return false;
		}

		if (found.distanceToSqr(escape.x, escape.y, escape.z) < found.distanceToSqr(mob)) {
			return false;
		}

		Path route = mob.world.getEntityPathToTilePos(mob,
			new TilePos(escape.x, escape.y, escape.z), PATH_SEARCH_RANGE);
		if (!reaches(route, escape)) {

			return false;
		}

		this.threat = found;
		this.plannedRoute = route;
		return true;
	}

	@Nullable
	private Entity findThreat(MobPathfinder mob) {
		if (this.threatClass == Player.class) {
			Player player = mob.world.getClosestPlayer(mob.x, mob.y, mob.z, this.triggerRange);

			return player != null && player.getGamemode().hasHostileMobs() ? player : null;
		}

		AABBd box = MathHelper.aabbGrow(mob.bb, this.triggerRange, SEARCH_HEIGHT, this.triggerRange,
			new AABBd());
		List<? extends Entity> found = mob.world.getEntitiesWithinAABB(this.threatClass, box);

		return found.isEmpty() ? null : found.get(0);
	}

	private static boolean reaches(@Nullable Path route, Vector3d goal) {
		if (route == null || route.length == 0) {
			return false;
		}
		Node last = route.last();
		return Math.abs(last.x - MathHelper.floor(goal.x)) <= DESTINATION_SLACK
			&& Math.abs(last.z - MathHelper.floor(goal.z)) <= DESTINATION_SLACK;
	}

	@Override
	public void startExecuting() {
		this.brain.setPath(this.plannedRoute);
	}

	@Override
	public boolean continueExecuting() {
		return this.host.asMob().getTarget() == null && !this.brain.noPath();
	}

	@Override
	public void resetTask() {
		this.threat = null;
		this.plannedRoute = null;
		this.brain.setPath(null);
	}

	@Override
	public float moveSpeed() {
		if (this.threat == null) {
			return this.farSpeed;
		}
		return this.host.asMob().distanceToSqr(this.threat) < SPRINT_RANGE_SQ
			? this.nearSpeed : this.farSpeed;
	}

	@Override
	public VanillaAI vanillaAI() {
		return VanillaAI.NO_ROAM;
	}

	@Nullable
	public Entity getThreat() {
		return this.threat;
	}
}
