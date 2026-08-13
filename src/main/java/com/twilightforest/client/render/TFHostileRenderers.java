package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFKobold;
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
public final class TFHostileRenderers {
	private TFHostileRenderers() {}

	public static final class Kobold extends MobRendererTFBiped<MobTFKobold> {
		public Kobold() {
			super("geometry.kobold", 0.0D, 0.5F);
		}
	}

	public static class Bug<T extends Mob> extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		private static final float SPLAY = 0.28559935F;

		private static final float MIDDLE_SPLAY = 0.74F;

		private static final float FAN = (float) (Math.PI / 8);

		private static final float SWING = 0.4F;

		private static final float RATE = 0.6662F;

		public Bug(String geometry, float shadowSize) {
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

			float strideRear = stride(limbSwing, 0.0F, limbYaw);
			float strideMiddle = stride(limbSwing, (float) Math.PI, limbYaw);
			float strideFront = stride(limbSwing, (float) (Math.PI * 3.0 / 2.0), limbYaw);

			float liftRear = lift(limbSwing, 0.0F, limbYaw);
			float liftMiddle = lift(limbSwing, (float) Math.PI, limbYaw);
			float liftFront = lift(limbSwing, (float) (Math.PI * 3.0 / 2.0), limbYaw);

			setLeg(model, "leg1", FAN * 2.0F + strideRear, -SPLAY + liftRear);
			setLeg(model, "leg2", -FAN * 2.0F - strideRear, SPLAY - liftRear);
			setLeg(model, "leg3", FAN + strideMiddle, -SPLAY * MIDDLE_SPLAY + liftMiddle);
			setLeg(model, "leg4", -FAN - strideMiddle, SPLAY * MIDDLE_SPLAY - liftMiddle);
			setLeg(model, "leg5", -FAN * 2.0F + strideFront, -SPLAY + liftFront);
			setLeg(model, "leg6", FAN * 2.0F - strideFront, SPLAY - liftFront);

			return model;
		}

		private static float stride(float limbSwing, float phase, float limbYaw) {
			return -(MathHelper.cos(limbSwing * RATE * 2.0F + phase) * SWING) * limbYaw;
		}

		private static float lift(float limbSwing, float phase, float limbYaw) {
			return Math.abs(MathHelper.sin(limbSwing * RATE + phase) * SWING) * limbYaw;
		}

		private static void setLeg(StaticEntityModel model, String bone, float rotY, float rotZ) {
			BoneTransform t = model.getTransform(bone);
			if (t != null) {
				t.rotY = rotY;
				t.rotZ = rotZ;
			}
		}
	}

	public static final class Swarm<T extends Mob> extends MobRenderer<T> {
		private static final String MODEL_KEY = "main";

		public Swarm(String geometry, float shadowSize) {
			super(shadowSize);
			setModel(MODEL_KEY, geometry, 0.0D);
		}

		private static void set(StaticEntityModel model, String bone, float x, float y, float z) {
			BoneTransform t = model.getTransform(bone);
			if (t != null) {
				t.rotX = x;
				t.rotY = y;
				t.rotZ = z;
			}
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

			float t = entity.tickCount + partialTick;
			set(model, "core",  MathHelper.sin(t / 5.0F) / 4.0F, t / 5.0F,
				MathHelper.cos(t / 5.0F) / 4.0F);
			set(model, "node1", MathHelper.sin(t / 6.0F) / 2.0F, t / 2.0F,
				MathHelper.cos(t / 5.0F) / 4.0F);
			set(model, "node2", t / 5.0F, MathHelper.sin(t / 2.0F) / 3.0F,
				MathHelper.cos(t / 5.0F) / 4.0F);
			set(model, "node3", MathHelper.cos(t / 4.0F) / 2.0F, MathHelper.sin(t / 7.0F) / 3.0F,
				t / 5.0F);
			set(model, "node4", t / 2.0F, MathHelper.sin(t / 5.0F) / 4.0F,
				MathHelper.sin(t / 6.0F) / 2.0F);
			set(model, "node5", MathHelper.cos(t / 5.0F) / 4.0F, MathHelper.cos(t / 5.0F) / 4.0F,
				MathHelper.sin(t / 2.0F) / 3.0F);
			set(model, "node6", MathHelper.cos(t / 4.0F) / 2.0F, t / 5.0F,
				MathHelper.cos(t / 7.0F) / 3.0F);
			return model;
		}
	}
}
