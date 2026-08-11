package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFBighorn;
import com.twilightforest.entity.MobTFBoar;
import com.twilightforest.entity.MobTFDeer;
import com.twilightforest.entity.MobTFHedgeSpider;
import com.twilightforest.entity.MobTFNaga;
import com.twilightforest.entity.MobTFNagaSegment;
import com.twilightforest.entity.MobTFPenguin;
import com.twilightforest.entity.MobTFRedcap;
import com.twilightforest.entity.MobTFSkeletonDruid;
import com.twilightforest.entity.MobTFSwarmSpider;
import com.twilightforest.entity.MobTFWraith;
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

		dispatcher.assignRenderer(MobTFPenguin.class, new TFMiscRenderers.Penguin());

		dispatcher.assignRenderer(MobTFSwarmSpider.class, new TFMiscRenderers.SwarmSpider());
		dispatcher.assignRenderer(MobTFHedgeSpider.class, new TFMiscRenderers.HedgeSpider());

		dispatcher.assignRenderer(MobTFRedcap.class, new TFHumanoidRenderers.Redcap());
		dispatcher.assignRenderer(MobTFSkeletonDruid.class, new TFHumanoidRenderers.SkeletonDruid());
		dispatcher.assignRenderer(MobTFWraith.class, new TFHumanoidRenderers.Wraith());

		dispatcher.assignRenderer(MobTFNaga.class, new TFMiscRenderers.Naga());
		dispatcher.assignRenderer(MobTFNagaSegment.class, new TFMiscRenderers.NagaSegment());

	}
}
