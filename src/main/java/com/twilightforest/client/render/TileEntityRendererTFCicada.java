package com.twilightforest.client.render;

import com.twilightforest.block.entity.TileEntityTFCicada;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public class TileEntityRendererTFCicada extends TileEntityRendererTFCritter<TileEntityTFCicada> {

	public static final String GEOMETRY = "geometry.cicada";

	public static final String TEXTURE = "/assets/twilightforest/textures/entity/cicada-model.png";

	public TileEntityRendererTFCicada() {
		super(GEOMETRY, TEXTURE);
	}

	@Override
	protected float currentYaw(TileEntityTFCicada tileEntity) {
		return tileEntity.currentYaw;
	}

	@Override
	protected void renderCritter(TessellatorGeneral tessellator, StaticEntityModel model,
	                             TileEntityTFCicada tileEntity, float partialTick) {
		model.render();
	}
}
