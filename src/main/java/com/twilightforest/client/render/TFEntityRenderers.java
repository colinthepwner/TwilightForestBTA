package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFBighorn;
import com.twilightforest.entity.MobTFBoar;
import com.twilightforest.entity.MobTFDeer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.EntityRendererDispatcher;

@Environment(EnvType.CLIENT)
public final class TFEntityRenderers {
	private TFEntityRenderers() {}

	public static void registerRenderers(EntityRendererDispatcher dispatcher) {
		dispatcher.assignRenderer(MobTFBoar.class, new MobRendererTFBoar());
		dispatcher.assignRenderer(MobTFBighorn.class, new MobRendererTFBighorn());
		dispatcher.assignRenderer(MobTFDeer.class, new MobRendererTFDeer());
	}
}
