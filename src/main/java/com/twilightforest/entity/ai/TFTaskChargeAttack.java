package com.twilightforest.entity.ai;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class TFTaskChargeAttack extends TFTask {

	public interface Charger extends TFBrainHost {

		void setCharging(boolean charging);

		boolean chargeAttack(Entity victim);
	}

	protected static final double MIN_RANGE_SQ = 16.0;

	protected static final double MAX_RANGE_SQ = 64.0;

	protected static final int FREQ = 1;

	protected static final double OVERSHOOT = 2.1;

	private static final int WINDUP_MIN = 15;
	private static final int WINDUP_SPREAD = 30;

	private static final float WINDUP_LEG_CHURN = 0.8F;

	private static final float PATH_SEARCH_RANGE = 16.0F;

	private static final float MAX_YAW_TURN = 10.0F;

	private static final float MAX_PITCH_TURN = 40.0F;

	protected final Charger charger;
	protected final TFBrain brain;
	private final float speed;

	@Nullable
	private Entity chargeTarget;

	private double chargeX;
	private double chargeY;
	private double chargeZ;

	protected int windup;

	protected boolean hasAttacked;

	public TFTaskChargeAttack(Charger charger, TFBrain brain, float speed) {
		this.charger = charger;
		this.brain = brain;
		this.speed = speed;

		this.setMutexBits(MUTEX_MOVE | MUTEX_LOOK);
	}

	@Override
	public boolean shouldExecute() {
		MobPathfinder mob = this.charger.asMob();

		Entity target = mob.getTarget();
		if (target == null) {
			return false;
		}

		double distanceSq = mob.distanceToSqr(target);
		if (distanceSq < MIN_RANGE_SQ || distanceSq > MAX_RANGE_SQ) {
			return false;
		}

		if (!mob.onGround) {
			return false;
		}

		Vector3d chargePos = findChargePoint(mob, target, OVERSHOOT);

		double eyeY = target.y + (target instanceof Mob m ? m.getHeadHeight() : 0.0);
		Vector3d eyes = new Vector3d(target.x, eyeY, target.z);
		if (mob.world.checkBlockCollisionBetweenPoints(eyes, chargePos) != null) {
			return false;
		}

		this.chargeTarget = target;
		this.chargeX = chargePos.x;
		this.chargeY = chargePos.y;
		this.chargeZ = chargePos.z;

		return true;
	}

	@Override
	public boolean continueExecuting() {
		return this.windup > 0 || !this.brain.noPath();
	}

	@Override
	public void startExecuting() {
		this.windup = WINDUP_MIN + this.charger.asMob().world.rand.nextInt(WINDUP_SPREAD);
	}

	@Override
	public void updateTask() {
		MobPathfinder mob = this.charger.asMob();

		this.lookAt(mob, this.chargeX, this.chargeY - 1.0, this.chargeZ);

		if (this.windup > 0) {
			if (--this.windup == 0) {

				Path route = mob.world.getEntityPathToTilePos(mob,
					new TilePos(this.chargeX, this.chargeY, this.chargeZ), PATH_SEARCH_RANGE);
				this.brain.setPath(route);
			} else {

				mob.walkAnimSpeed += WINDUP_LEG_CHURN;
				this.charger.setCharging(true);
			}
		}

		if (this.chargeTarget == null) {
			return;
		}

		double reach = mob.bbWidth * (float) OVERSHOOT;
		double reachSq = reach * reach;

		double contactSq = mob.distanceToSqr(this.chargeTarget.x, this.chargeTarget.bb.minY,
			this.chargeTarget.z);

		if (contactSq <= reachSq && !this.hasAttacked) {
			this.hasAttacked = true;
			this.charger.chargeAttack(this.chargeTarget);
		}
	}

	@Override
	public void resetTask() {
		this.windup = 0;
		this.chargeTarget = null;
		this.hasAttacked = false;
		this.charger.setCharging(false);

		this.brain.setPath(null);
	}

	@Override
	public float moveSpeed() {
		return this.speed;
	}

	@Override
	public VanillaAI vanillaAI() {
		return VanillaAI.NONE;
	}

	@Nullable
	public Entity getChargeTarget() {
		return this.chargeTarget;
	}

	public int getWindup() {
		return this.windup;
	}

	protected static Vector3d findChargePoint(Entity attacker, Entity target, double overshoot) {
		double vecX = target.x - attacker.x;
		double vecZ = target.z - attacker.z;
		float angle = (float) Math.atan2(vecZ, vecX);
		double distance = Math.sqrt(vecX * vecX + vecZ * vecZ);

		double dx = MathHelper.cos(angle) * (distance + overshoot);
		double dz = MathHelper.sin(angle) * (distance + overshoot);

		return new Vector3d(attacker.x + dx, target.y, attacker.z + dz);
	}

	private void lookAt(MobPathfinder mob, double x, double y, double z) {
		double dx = x - mob.x;
		double dz = z - mob.z;
		double dy = y - (mob.y + mob.getHeadHeight());
		double flat = Math.sqrt(dx * dx + dz * dz);

		float wantYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float turn = wrapDegrees(wantYaw - mob.yRot);
		mob.yRot += MathHelper.clamp(turn, -MAX_YAW_TURN, MAX_YAW_TURN);

		float wantPitch = (float) (-(Math.atan2(dy, flat) * 180.0 / Math.PI));
		float tilt = wrapDegrees(wantPitch - mob.xRot);
		mob.xRot += MathHelper.clamp(tilt, -MAX_PITCH_TURN, MAX_PITCH_TURN);
	}

	private static float wrapDegrees(float degrees) {
		while (degrees < -180.0F) {
			degrees += 360.0F;
		}
		while (degrees >= 180.0F) {
			degrees -= 360.0F;
		}
		return degrees;
	}
}
