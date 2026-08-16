package com.twilightforest.client.render;

import com.twilightforest.entity.EntityTFTinyFirefly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Lighting;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RendererTFTinyFirefly extends EntityRenderer<EntityTFTinyFirefly> {

	static final String TEXTURE = "/assets/twilightforest/textures/entity/firefly-tiny.png";

	static final float QUAD_SIZE = 0.625F;

	private static final double U0 = 30.0D / 64.0D;
	private static final double U1 = 40.0D / 64.0D;
	private static final double V0 = 0.0D / 32.0D;
	private static final double V1 = 10.0D / 32.0D;

	private static final float ALPHA_TEST = 0.0F;

	public RendererTFTinyFirefly() {

		super(0.0F);
	}

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull EntityTFTinyFirefly firefly,
	                   double x, double y, double z, float yaw, float partialTick) {
		this.bindTexture(TEXTURE);
		drawGlowQuad(tessellator, x, y, z, firefly.getGlowBrightness(), firefly.glowSize,
			this.renderDispatcher.viewLerpYaw, this.renderDispatcher.viewLerpPitch);
	}

	static void drawGlowQuad(TessellatorGeneral tessellator, double x, double y, double z,
	                         float brightness, float size, float viewYaw, float viewPitch) {
		if (brightness <= 0.0F) {
			return;
		}

		boolean wasLit = GLRenderer.globalGetLightEnabled();
		boolean wasLight0 = GLRenderer.globalGetLight0().enabled;
		boolean wasLight1 = GLRenderer.globalGetLight1().enabled;

		GLRenderer.pushFrame();

		GLRenderer.setShader(Shaders.WORLD);

		GLRenderer.modelM4f().translate((float) x, (float) y + 0.5F, (float) z);

		GLRenderer.modelM4f().rotateY((float) Math.toRadians(-viewYaw));
		GLRenderer.modelM4f().rotateX((float) Math.toRadians(viewPitch));

		Lighting.disable();

		GLRenderer.setAlphaTest(ALPHA_TEST);
		GLRenderer.enableState(State.BLEND);

		GLRenderer.disableState(State.CULL_FACE);

		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, Math.min(brightness, 1.0F));

		float half = QUAD_SIZE * size * 0.5F;

		tessellator.startDrawingQuads();

		tessellator.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		tessellator.setNormal(0.0F, 0.0F, 1.0F);

		tessellator.setTextureUV(U0, V1);
		tessellator.addVertex(-half, -half, 0.0);
		tessellator.setTextureUV(U1, V1);
		tessellator.addVertex(half, -half, 0.0);
		tessellator.setTextureUV(U1, V0);
		tessellator.addVertex(half, half, 0.0);
		tessellator.setTextureUV(U0, V0);
		tessellator.addVertex(-half, half, 0.0);
		tessellator.draw();

		GLRenderer.popFrame();

		GLRenderer.globalSetLightEnabled(wasLit);
		GLRenderer.globalGetLight0().enabled = wasLight0;
		GLRenderer.globalGetLight1().enabled = wasLight1;
	}
}
