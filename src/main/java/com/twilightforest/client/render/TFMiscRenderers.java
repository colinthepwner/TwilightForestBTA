package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFHedgeSpider;
import com.twilightforest.entity.MobTFNaga;
import com.twilightforest.entity.MobTFNagaSegment;
import com.twilightforest.entity.MobTFPenguin;
import com.twilightforest.entity.MobTFSwarmSpider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public final class TFMiscRenderers {
	private TFMiscRenderers() {}

	public static final class Penguin extends MobRenderer<MobTFPenguin> {
		private static final String MODEL_KEY = "main";

		public Penguin() {
			super(0.3F);
			setModel(MODEL_KEY, "geometry.penguin", 0.0D);
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull MobTFPenguin entity, float brightness,
		                                                     float partialTick, int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model == null) {
				return null;
			}
			model.resetBones();

			float bodyYaw = this.getBodyYaw(entity, partialTick);
			float headYaw = this.getHeadYaw(entity, partialTick) - bodyYaw;
			float headPitch = this.getHeadPitch(entity, partialTick);

			BoneTransform head = model.getTransform("head");
			if (head != null) {
				head.rotX = headPitch * MathHelper.DEG_TO_RAD;
				head.rotY = headYaw * MathHelper.DEG_TO_RAD;
			}

			float limbSwing = this.getLimbSwing(entity, partialTick);
			float limbYaw = this.getLimbYaw(entity, partialTick);

			setRotX(model, "legRight", MathHelper.cos(limbSwing) * 0.7F * limbYaw);
			setRotX(model, "legLeft", MathHelper.cos(limbSwing + (float) Math.PI) * 0.7F * limbYaw);

			float flap = entity.oFlap + (entity.flap - entity.oFlap) * partialTick;
			setRotZ(model, "wingRight", flap);
			setRotZ(model, "wingLeft", -flap);
			return model;
		}
	}

	public static class Spider<T extends Mob> extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		public Spider(double scale, float shadowSize) {
			super(shadowSize);
			setModel(MODEL_KEY, "geometry.spider", 0.0D);
			this.scale = scale;
		}

		private final double scale;

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
			float headYaw = this.getHeadYaw(entity, partialTick) - bodyYaw;
			float headPitch = this.getHeadPitch(entity, partialTick);

			BoneTransform head = model.getTransform("head");
			if (head != null) {
				head.rotX = headPitch * MathHelper.DEG_TO_RAD;
				head.rotY = headYaw * MathHelper.DEG_TO_RAD;
			}

			float limbSwing = this.getLimbSwing(entity, partialTick);
			float limbYaw = this.getLimbYaw(entity, partialTick);

			double roll = 45.0F * MathHelper.DEG_TO_RAD;
			double yaw = 22.5 * MathHelper.DEG_TO_RAD;
			double[] rollScale = {1.0, 0.74, 0.74, 1.0};
			double[] yawScale = {2.0, 1.0, -1.0, -2.0};
			double[] phase = {0.0, Math.PI, Math.PI / 2.0, Math.PI * 3.0 / 2.0};

			for (int pair = 0; pair < 4; pair++) {
				BoneTransform left = model.getTransform("leg" + (pair * 2));
				BoneTransform right = model.getTransform("leg" + (pair * 2 + 1));
				if (left == null || right == null) continue;

				double step = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + (float) phase[pair]) * 0.4F) * limbYaw;
				double lift = Math.abs(MathHelper.sin(limbSwing * 0.6662F + (float) phase[pair]) * 0.4F) * limbYaw;

				left.rotZ = -roll * rollScale[pair] + lift;
				right.rotZ = roll * rollScale[pair] - lift;
				left.rotY = yaw * yawScale[pair] + step;
				right.rotY = -yaw * yawScale[pair] - step;
			}
			return model;
		}

		@Override
		protected void preRenderTransform(@NotNull T entity, double x, double y, double z,
		                                  float yaw, float partialTick) {
			super.preRenderTransform(entity, x, y, z, yaw, partialTick);
			GLRenderer.modelM4f().scale((float) this.scale);
		}
	}

	public static final class SwarmSpider extends Spider<MobTFSwarmSpider> {
		public SwarmSpider() {
			super(0.5D, 0.5F);
		}
	}

	public static final class HedgeSpider extends Spider<MobTFHedgeSpider> {
		public HedgeSpider() {
			super(1.0D, 1.0F);
		}
	}

	public static class NagaPart<T extends Mob> extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		public NagaPart(String modelId, float shadowSize) {
			super(shadowSize);
			setModel(MODEL_KEY, modelId, 0.0D);
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull T entity, float brightness,
		                                                     float partialTick, int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model != null) {
				model.resetBones();
			}
			return model;
		}

		@Override
		protected void preRenderTransform(@NotNull T entity, double x, double y, double z,
		                                  float yaw, float partialTick) {
			super.preRenderTransform(entity, x, y, z, yaw, partialTick);
			GLRenderer.modelM4f().scale(2.0F);
		}
	}

	public static final class Naga extends NagaPart<MobTFNaga> {
		public Naga() {
			super("geometry.naga", 1.5F);
		}
	}

	public static final class NagaSegment extends NagaPart<MobTFNagaSegment> {
		public NagaSegment() {
			super("geometry.nagasegment", 1.5F);
		}
	}

	static void setRotX(StaticEntityModel model, String bone, float angle) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX = angle;
		}
	}

	static void setRotZ(StaticEntityModel model, String bone, float angle) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotZ = angle;
		}
	}
}
