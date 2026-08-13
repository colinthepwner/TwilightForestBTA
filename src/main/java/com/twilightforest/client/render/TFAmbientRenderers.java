package com.twilightforest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public final class TFAmbientRenderers {
	private TFAmbientRenderers() {}

	static final float LIMB_FREQ = 0.6662F;

	public static class Critter<T extends Mob> extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		public Critter(String geometry, float shadowSize) {
			super(shadowSize);
			setModel(MODEL_KEY, geometry, 0.0D);
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull T entity, float brightness,
		                                                     float partialTick, int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model == null) {
				return null;
			}
			model.resetBones();

			float bodyYaw = this.getBodyYaw(entity, partialTick);
			BoneTransform head = model.getTransform("head");
			if (head != null) {
				head.rotX = this.getHeadPitch(entity, partialTick) * MathHelper.DEG_TO_RAD;
				head.rotY = (this.getHeadYaw(entity, partialTick) - bodyYaw) * MathHelper.DEG_TO_RAD;
			}

			float limbSwing = this.getLimbSwing(entity, partialTick);
			float limbYaw = this.getLimbYaw(entity, partialTick);

			TFMiscRenderers.setRotX(model, "legFrontRight", MathHelper.cos(limbSwing * LIMB_FREQ) * 1.4F * limbYaw);
			TFMiscRenderers.setRotX(model, "legBackLeft", MathHelper.cos(limbSwing * LIMB_FREQ) * 1.4F * limbYaw);
			TFMiscRenderers.setRotX(model, "legFrontLeft",
				MathHelper.cos(limbSwing * LIMB_FREQ + (float) Math.PI) * 1.4F * limbYaw);
			TFMiscRenderers.setRotX(model, "legBackRight",
				MathHelper.cos(limbSwing * LIMB_FREQ + (float) Math.PI) * 1.4F * limbYaw);
			return model;
		}
	}

	public static class Bird<T extends net.minecraft.core.entity.animal.MobChicken>
		extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		public Bird(String geometry, float shadowSize) {
			super(shadowSize);
			setModel(MODEL_KEY, geometry, 0.0D);
		}

		@Override
		protected float getLimbPitch(@NotNull T entity, float partialTick) {
			float cycle = MathHelper.lerp(entity.oFlap, entity.flap, partialTick);
			float amplitude = MathHelper.lerp(entity.oFlapSpeed, entity.flapSpeed, partialTick);
			return (MathHelper.sin(cycle) + 1.0F) * amplitude;
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull T entity, float brightness,
		                                                     float partialTick, int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model == null) {
				return null;
			}
			model.resetBones();

			float bodyYaw = this.getBodyYaw(entity, partialTick);
			BoneTransform head = model.getTransform("head");
			if (head != null) {
				head.rotX = this.getHeadPitch(entity, partialTick) * MathHelper.DEG_TO_RAD;
				head.rotY = (this.getHeadYaw(entity, partialTick) - bodyYaw) * MathHelper.DEG_TO_RAD;
			}

			float limbSwing = this.getLimbSwing(entity, partialTick);
			float limbYaw = this.getLimbYaw(entity, partialTick);
			TFMiscRenderers.setRotX(model, "legRight", MathHelper.cos(limbSwing * LIMB_FREQ) * 1.4F * limbYaw);
			TFMiscRenderers.setRotX(model, "legLeft",
				MathHelper.cos(limbSwing * LIMB_FREQ + (float) Math.PI) * 1.4F * limbYaw);

			float flap = this.getLimbPitch(entity, partialTick);
			TFMiscRenderers.setRotZ(model, "wingRight", flap);
			TFMiscRenderers.setRotZ(model, "wingLeft", -flap);
			return model;
		}
	}
}
