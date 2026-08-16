package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFMinoshroom;
import com.twilightforest.entity.MobTFQuestRam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.util.helper.MathHelper;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public final class TFBossRenderers {
	private TFBossRenderers() {}

	private static final float RATE = 0.6662F;

	private static final float SPINE = (float) (Math.PI / 2.0);

	public static final class Minoshroom extends MobRendererTFBiped<MobTFMinoshroom> {

		private static final float LEG_SWING = 1.4F;

		private static final float SWAY_Z_RATE = 0.09F;
		private static final float SWAY_X_RATE = 0.067F;
		private static final float SWAY_AMPLITUDE = 0.05F;

		public Minoshroom() {
			super("geometry.minoshroom", 0.0D, 0.625F);
		}

		@Override
		protected void poseExtra(StaticEntityModel model, MobTFMinoshroom entity, float limbSwing,
		                         float limbYaw, float partialTick) {

			setRotX(model, "barrel", SPINE);
			setRotX(model, "udders", SPINE);

			float phaseA = MathHelper.cos(limbSwing * RATE) * LEG_SWING * limbYaw;
			float phaseB = MathHelper.cos(limbSwing * RATE + (float) Math.PI) * LEG_SWING * limbYaw;
			setRotX(model, "leg0", phaseA);
			setRotX(model, "leg1", phaseB);
			setRotX(model, "leg2", phaseB);
			setRotX(model, "leg3", phaseA);

			float age = entity.tickCount + partialTick;
			float lift = MathHelper.cos(age * SWAY_Z_RATE) * SWAY_AMPLITUDE + SWAY_AMPLITUDE;
			float twist = MathHelper.sin(age * SWAY_X_RATE) * SWAY_AMPLITUDE;
			addRotation(model, "armRight", twist, lift);
			addRotation(model, "armLeft", -twist, -lift);
		}
	}

	public static final class QuestRam extends MobRendererTFQuadruped<MobTFQuestRam> {

		private static final float STRIDE = 0.7F;

		public QuestRam() {
			super("geometry.questram", 0.0D, 1.0F);
		}

		@Override
		protected float limbSwingAmplitude(MobTFQuestRam entity, float partialTick) {
			return STRIDE;
		}

		@Override
		protected void poseExtra(StaticEntityModel model, MobTFQuestRam entity, float limbSwing,
		                         float limbYaw, float partialTick) {

			BoneTransform head = model.getTransform("head");
			BoneTransform neck = model.getTransform("neck");
			if (head != null && neck != null) {
				neck.rotY = head.rotY;
			}

			copyRotX(model, "leg0", "haunch0");
			copyRotX(model, "leg1", "haunch1");
			copyRotX(model, "leg2", "haunch2");
			copyRotX(model, "leg3", "haunch3");
		}

		private static void copyRotX(StaticEntityModel model, String from, String to) {
			BoneTransform source = model.getTransform(from);
			BoneTransform target = model.getTransform(to);
			if (source != null && target != null) {
				target.rotX = source.rotX;
			}
		}
	}

	private static void addRotation(StaticEntityModel model, String bone, float dx, float dz) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX += dx;
			t.rotZ += dz;
		}
	}
}
