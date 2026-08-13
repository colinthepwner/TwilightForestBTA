package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.enums.MobCategory;
import net.minecraft.core.world.SpawnerMobs;
import net.minecraft.core.world.config.spawning.SpawnerConfig;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnerMobs.class)
public class AmbientSpawnCapMixin {

	private static final int AMBIENT_BUDGET = 100;

	@Redirect(
		method = "performSpawning",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/core/enums/MobCategory;getMaxCreaturesPerChunk()I"))
	private static int twilightforest$restoreAmbientBudget(MobCategory category, World world,
	                                                       SpawnerConfig spawnerConfig) {
		if (category == MobCategory.AMBIENT
			&& world.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST) {
			return AMBIENT_BUDGET;
		}
		return category.getMaxCreaturesPerChunk();
	}
}
