package com.twilightforest.entity.ai;

import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Random;

public final class TFRandomPositionGenerator {

	private static final int TRIES = 10;

	private TFRandomPositionGenerator() {
	}

	@Nullable
	public static Vector3d findRandomTarget(TFBrainHost host, int xz, int y) {
		return find(host, xz, y, null);
	}

	@Nullable
	public static Vector3d findRandomTargetAwayFrom(TFBrainHost host, int xz, int y, Vector3d awayFrom) {
		return find(host, xz, y, awayFrom);
	}

	@Nullable
	private static Vector3d find(TFBrainHost host, int xz, int y, @Nullable Vector3d awayFrom) {
		MobPathfinder mob = host.asMob();
		Random random = mob.world.rand;

		double currentDistSq = awayFrom == null ? 0.0 : awayFrom.distanceSquared(mob.x, mob.y, mob.z);

		boolean found = false;
		int bestX = 0;
		int bestY = 0;
		int bestZ = 0;
		float bestWeight = -99999.0F;

		TilePos probe = new TilePos(0, 0, 0);

		for (int i = 0; i < TRIES; i++) {
			int cx = MathHelper.floor(mob.x + random.nextInt(2 * xz + 1) - xz);
			int cy = MathHelper.floor(mob.y + random.nextInt(2 * y + 1) - y);
			int cz = MathHelper.floor(mob.z + random.nextInt(2 * xz + 1) - xz);

			if (awayFrom != null
					&& awayFrom.distanceSquared(cx + 0.5, cy + 0.5, cz + 0.5) < currentDistSq) {
				continue;
			}

			float weight = host.tfBlockPathWeight(probe.set(cx, cy, cz));
			if (weight > bestWeight) {
				bestWeight = weight;
				bestX = cx;
				bestY = cy;
				bestZ = cz;
				found = true;
			}
		}

		return found ? new Vector3d(bestX + 0.5, bestY, bestZ + 0.5) : null;
	}

	public static Vector3d at(net.minecraft.core.entity.Entity entity) {
		return new Vector3d(entity.x, entity.y, entity.z);
	}
}
