package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFBoar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MobRendererTFBoar extends MobRendererTFQuadruped<MobTFBoar> {
	public MobRendererTFBoar() {
		super("geometry.wildboar", 0.0D, 0.7F);
	}
}
