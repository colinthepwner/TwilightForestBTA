package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.entity.animal.MobFireflyCluster;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobFireflyCluster.class)
public class FireflySpawnMixin {

	private static final int THINNING = 8;

	private static final int REFUSE = 5;

	@Redirect(
		method = "canSpawnHere",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/core/world/World;getBlockLightValue(III)I"))
	private int twilightforest$duskIsDarkEnough(World world, int x, int y, int z) {
		if (world.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST) {
			return world.rand.nextInt(THINNING) == 0 ? REFUSE : 0;
		}
		return world.getBlockLightValue(x, y, z);
	}
}
