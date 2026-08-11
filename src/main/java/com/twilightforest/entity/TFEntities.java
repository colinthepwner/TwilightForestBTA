package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.projectile.EntityTFNatureBolt;
import com.twilightforest.entity.projectile.EntityTFProjectile;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.factories.EntityFactory;
import net.minecraft.core.util.collection.NamespaceID;

import java.util.ArrayList;
import java.util.List;

import static com.twilightforest.TwilightForest.MOD_ID;

public final class TFEntities {
	private TFEntities() {}

	public static final List<String> REGISTERED_IDS = new ArrayList<>();

	private static <T extends Entity> void register(Class<T> entityClass, String name,
	                                                EntityFactory<T> factory) {
		String nameKey = "guidebook.section.mob." + name + ".name";
		EntityDispatcher.getInstance().addMapping(
			entityClass,
			NamespaceID.getPermanent(MOD_ID, name),
			factory,
			nameKey
		);
		REGISTERED_IDS.add(name);
	}

	public static void initEntities() {

		register(MobTFBoar.class, "wildboar", MobTFBoar::new);
		register(MobTFBighorn.class, "bighorn", MobTFBighorn::new);
		register(MobTFDeer.class, "wilddeer", MobTFDeer::new);

		register(MobTFPenguin.class, "penguin", MobTFPenguin::new);
		register(MobTFSwarmSpider.class, "swarmspider", MobTFSwarmSpider::new);
		register(MobTFHedgeSpider.class, "hedgespider", MobTFHedgeSpider::new);

		register(MobTFRedcap.class, "redcap", MobTFRedcap::new);
		register(MobTFSkeletonDruid.class, "skeletondruid", MobTFSkeletonDruid::new);
		register(MobTFWraith.class, "wraith", MobTFWraith::new);

		register(MobTFHostileWolf.class, "hostilewolf", MobTFHostileWolf::new);

		register(EntityTFProjectile.class, "projectile", EntityTFProjectile::new);
		register(EntityTFNatureBolt.class, "naturebolt", EntityTFNatureBolt::new);

		register(MobTFNaga.class, "naga", MobTFNaga::new);
		register(MobTFNagaSegment.class, "nagasegment", MobTFNagaSegment::new);

		TwilightForest.LOGGER.info("Registered {} Twilight Forest entities.", REGISTERED_IDS.size());
	}
}
