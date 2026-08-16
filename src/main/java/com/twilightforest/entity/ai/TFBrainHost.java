package com.twilightforest.entity.ai;

import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.world.pos.TilePosc;

public interface TFBrainHost {

	MobPathfinder asMob();

	float tfBlockPathWeight(TilePosc pos);

	void tfSetSpeed(float speed);

	void tfSetRandomWalk(boolean enabled);

	void tfDrive(float yRot, float moveForward, boolean jumping);
}
