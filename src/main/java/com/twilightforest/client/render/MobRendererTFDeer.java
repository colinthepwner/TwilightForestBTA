package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFDeer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MobRendererTFDeer extends MobRendererTFQuadruped<MobTFDeer> {
	public MobRendererTFDeer() {
		super("geometry.wilddeer", 0.0D, 0.7F);
	}
}
