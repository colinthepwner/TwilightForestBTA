package com.twilightforest.client.render;

import com.twilightforest.block.entity.TileEntityTFFirefly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public class TileEntityRendererTFFirefly extends TileEntityRendererTFCritter<TileEntityTFFirefly> {

	public static final String GEOMETRY = "geometry.firefly";

	public static final String TEXTURE = "/assets/twilightforest/textures/entity/firefly-tiny.png";

	public TileEntityRendererTFFirefly() {
		super(GEOMETRY, TEXTURE);
	}

	@Override
	protected float currentYaw(TileEntityTFFirefly tileEntity) {
		return tileEntity.currentYaw;
	}

	@Override
	protected void renderCritter(TessellatorGeneral tessellator, StaticEntityModel model,
	                             TileEntityTFFirefly tileEntity, float partialTick) {

		GLRenderer.disableState(State.BLEND);
		model.render();
		GLRenderer.enableState(State.BLEND);

		BlendFactor oldSrc = GLRenderer.getBlendSFactor();
		BlendFactor oldDst = GLRenderer.getBlendDFactor();
		float oldAlphaTest = GLRenderer.getAlphaTest();

		GLRenderer.setAlphaTest(0.0F);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, tileEntity.glowIntensity);
		model.render();

		GLRenderer.setBlendFunc(oldSrc, oldDst);
		GLRenderer.setAlphaTest(oldAlphaTest);
		GLRenderer.disableState(State.BLEND);
	}
}
