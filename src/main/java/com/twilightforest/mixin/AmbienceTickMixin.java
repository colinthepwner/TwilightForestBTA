package com.twilightforest.mixin;

import com.twilightforest.client.sound.TFAmbience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class AmbienceTickMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void twilightforest$tickAmbience(CallbackInfo ci) {
		TFAmbience.tick(Minecraft.getMinecraft());
	}
}
