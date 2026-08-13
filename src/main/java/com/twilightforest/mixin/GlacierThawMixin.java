package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.block.BlockLogicIce;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockLogicIce.class)
public class GlacierThawMixin {

	@Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
	private void twilightforest$neverThaw(World world, TilePosc pos, Random rand, boolean randomTick,
	                                      CallbackInfo ci) {
		if (world != null && world.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST) {
			ci.cancel();
		}
	}
}
