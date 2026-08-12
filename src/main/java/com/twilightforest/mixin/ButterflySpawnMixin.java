package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.entity.animal.MobButterfly;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobButterfly.class)
public class ButterflySpawnMixin {

	@Redirect(
		method = "canSpawnHere",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/core/world/World;isDaytime()Z"))
	private boolean twilightforest$duskCountsAsDay(World world) {
		if (world.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST) {
			return true;
		}
		return world.isDaytime();
	}
}
