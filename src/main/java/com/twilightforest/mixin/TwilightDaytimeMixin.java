package com.twilightforest.mixin;

import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class TwilightDaytimeMixin {

	@Inject(method = "isDaytime", at = @At("HEAD"), cancellable = true)
	private void twilightforest$neverDaytime(CallbackInfoReturnable<Boolean> cir) {
		World self = (World) (Object) this;
		if (self.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST) {
			cir.setReturnValue(false);
		}
	}
}
