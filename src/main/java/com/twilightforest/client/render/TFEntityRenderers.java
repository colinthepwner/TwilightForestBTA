package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFBighorn;
import com.twilightforest.entity.MobTFBoar;
import com.twilightforest.entity.MobTFDeer;
import com.twilightforest.entity.EntityTFTinyFirefly;
import com.twilightforest.entity.MobTFHedgeSpider;
import com.twilightforest.entity.MobTFLich;
import com.twilightforest.entity.MobTFNaga;
import com.twilightforest.entity.MobTFNagaSegment;
import com.twilightforest.entity.MobTFBunny;
import com.twilightforest.entity.MobTFFireBeetle;
import com.twilightforest.entity.MobTFKobold;
import com.twilightforest.entity.MobTFMosquitoSwarm;
import com.twilightforest.entity.MobTFPinchBeetle;
import com.twilightforest.entity.MobTFRedcapSapper;
import com.twilightforest.entity.MobTFSlimeBeetle;
import com.twilightforest.entity.MobTFPenguin;
import com.twilightforest.entity.MobTFRaven;
import com.twilightforest.entity.MobTFSquirrel;
import com.twilightforest.entity.MobTFTinyBird;
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

		dispatcher.assignRenderer(MobTFLich.class, new TFHumanoidRenderers.Lich());

		dispatcher.assignRenderer(EntityTFTinyFirefly.class, new RendererTFTinyFirefly());

		dispatcher.assignRenderer(MobTFBunny.class,
			new TFAmbientRenderers.Critter<>("geometry.bunny", 0.3F));
		dispatcher.assignRenderer(MobTFSquirrel.class,
			new TFAmbientRenderers.Critter<>("geometry.squirrel", 0.3F));
		dispatcher.assignRenderer(MobTFTinyBird.class,
			new TFAmbientRenderers.Bird<>("geometry.tinybird", 0.3F));
		dispatcher.assignRenderer(MobTFRaven.class,
			new TFAmbientRenderers.Bird<>("geometry.raven", 0.3F));

		dispatcher.assignRenderer(MobTFKobold.class, new TFHostileRenderers.Kobold());
		dispatcher.assignRenderer(MobTFRedcapSapper.class, new TFHumanoidRenderers.Redcap());
		dispatcher.assignRenderer(MobTFFireBeetle.class,
			new TFHostileRenderers.Bug<>("geometry.firebeetle", 0.6F));
		dispatcher.assignRenderer(MobTFSlimeBeetle.class,
			new TFHostileRenderers.Bug<>("geometry.slimebeetle", 0.5F));
		dispatcher.assignRenderer(MobTFPinchBeetle.class,
			new TFHostileRenderers.Bug<>("geometry.pinchbeetle", 0.7F));
		dispatcher.assignRenderer(MobTFMosquitoSwarm.class,
			new TFHostileRenderers.Swarm<>("geometry.mosquitoswarm", 0.5F));

		dispatcher.assignRenderer(MobTFNaga.class, new TFMiscRenderers.Naga());
		dispatcher.assignRenderer(MobTFNagaSegment.class, new TFMiscRenderers.NagaSegment());

	}
}
