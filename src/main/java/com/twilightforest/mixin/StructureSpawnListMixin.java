package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.world.SpawnerMobs;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.config.spawning.SpawnerConfig;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnerMobs.class)
public class StructureSpawnListMixin {

	@Redirect(
		method = "performSpawning",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/core/world/World;getBlockBiome(Lnet/minecraft/core/world/pos/TilePosc;)Lnet/minecraft/core/world/biome/Biome;"))
	private static Biome twilightforest$structureGarrison(World instance, TilePosc pos,
	                                                      World world, SpawnerConfig spawnerConfig) {
		Biome actual = instance.getBlockBiome(pos);
		if (instance.getWorldType() != WorldTypeTwilightForest.TWILIGHT_FOREST) {
			return actual;
		}
		return StructureSpawnLists.biomeFor(instance, actual, pos.x(), pos.z());
	}
}
