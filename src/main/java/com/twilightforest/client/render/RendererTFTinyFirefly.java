package com.twilightforest.client.render;

import com.twilightforest.entity.EntityTFTinyFirefly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.DrawMode;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class RendererTFTinyFirefly extends EntityRenderer<EntityTFTinyFirefly> {

	private static final String TEXTURE = "/assets/twilightforest/textures/entity/firefly-tiny.png";

	private static final float QUAD_SIZE = 0.625F;

	public RendererTFTinyFirefly() {

		super(0.0F);
	}

	@Override
	public void render(@NotNull TessellatorGeneral tessellator, @NotNull EntityTFTinyFirefly firefly,
	                   double x, double y, double z, float yaw, float partialTick) {
		float alpha = firefly.getGlowBrightness();
		if (alpha <= 0.0F) {
			return;
		}

		Matrix4f model = GLRenderer.modelM4f();

		Matrix4f saved = new Matrix4f(model);

		model.translate((float) x, (float) y, (float) z);

		model.m00(1.0F); model.m01(0.0F); model.m02(0.0F);
		model.m10(0.0F); model.m11(1.0F); model.m12(0.0F);
		model.m20(0.0F); model.m21(0.0F); model.m22(1.0F);

		this.bindTexture(TEXTURE);

		GLRenderer.enableState(State.BLEND);

		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, alpha);

		float half = QUAD_SIZE * firefly.glowSize * 0.5F;
		tessellator.startDrawing(DrawMode.QUADS);
		tessellator.setTextureUV(0.0, 1.0);
		tessellator.addVertex(-half, -half, 0.0);
		tessellator.setTextureUV(1.0, 1.0);
		tessellator.addVertex(half, -half, 0.0);
		tessellator.setTextureUV(1.0, 0.0);
		tessellator.addVertex(half, half, 0.0);
		tessellator.setTextureUV(0.0, 0.0);
		tessellator.addVertex(-half, half, 0.0);
		tessellator.draw();

		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
		GLRenderer.disableState(State.BLEND);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		model.set(saved);
	}
}
