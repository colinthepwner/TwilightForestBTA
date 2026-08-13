package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFMobileFirefly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RendererTFMobileFirefly extends EntityRenderer<MobTFMobileFirefly> {

	private static final float GLOW_SIZE = 1.0F;

	public RendererTFMobileFirefly() {

		super(0.0F);
	}

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull MobTFMobileFirefly firefly,
	                   double x, double y, double z, float yaw, float partialTick) {
		this.bindTexture(RendererTFTinyFirefly.TEXTURE);
		RendererTFTinyFirefly.drawGlowQuad(tessellator, x, y, z,
			firefly.getGlowBrightness(), GLOW_SIZE,
			this.renderDispatcher.viewLerpYaw, this.renderDispatcher.viewLerpPitch);
	}
}
