package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFTowerGhast;
import com.twilightforest.entity.MobTFTowerGolem;
import com.twilightforest.entity.MobTFTowerTermite;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public final class TFTowerRenderers {
	private TFTowerRenderers() {}

	private static final String MODEL_KEY = "main";

	private static void setRot(StaticEntityModel model, String bone, double x, double y, double z) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX = x;
			t.rotY = y;
			t.rotZ = z;
		}
	}

	public static class Ghast<T extends MobTFTowerGhast> extends MobRenderer<T> {

		private static final int TENTACLES = 9;

		private static final float WAVE_AMPLITUDE = 0.2F;
		private static final float WAVE_RATE = 0.3F;
		private static final float WAVE_REST = 0.4F;

		private static final float FULL_CHARGE = 20.0F;

		private final float ghastScale;

		public Ghast(float shadowSize, float ghastScale) {
			super(shadowSize);
			this.ghastScale = ghastScale;
			setModel(MODEL_KEY, "geometry.ghast", 0.0D);
		}

		public static <T extends MobTFTowerGhast> Ghast<T> unscaled(float shadowSize) {
			return new Ghast<>(shadowSize, 0.0F);
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

			float charge = MathHelper.lerp(entity.attackChargeO, entity.attackCharge, partialTick)
				/ FULL_CHARGE;
			if (charge < 0.0F) {
				charge = 0.0F;
			}
			charge = 1.0F / (charge * charge * charge * charge * charge * 2.0F + 1.0F);

			if (this.ghastScale > 0.0F) {
				BoneTransform body = model.getTransform("body");
				if (body != null) {
					body.scaleY = (this.ghastScale + charge) / 2.0F;
					body.scaleX = body.scaleZ = (this.ghastScale + 1.0F / charge) / 2.0F;
				}
			}

			float t = this.getLimbPitch(entity, partialTick);
			for (int i = 0; i < TENTACLES; i++) {
				setRot(model, "tentacles_" + i,
					WAVE_AMPLITUDE * MathHelper.sin(t * WAVE_RATE + i) + WAVE_REST, 0.0D, 0.0D);
			}
			return model;
		}
	}

	public static final class Golem extends MobRenderer<MobTFTowerGolem> {

		private static final float WALK_PERIOD = 13.0F;

		private static final float ATTACK_PERIOD = 10.0F;

		private static final float LURCH_PERIOD = 13.0F;
		private static final float LURCH_DEGREES = 6.5F;

		private static final double LURCH_THRESHOLD = 0.01;

		private static final float ARM_SWING = 1.5F;
		private static final float ARM_REST = -0.2F;

		private static final float ARM_ATTACK_REST = -2.0F;

		private static final float LURCH_PHASE = 6.0F;

		public Golem() {
			super(0.5F);
			setModel(MODEL_KEY, "geometry.towergolem", 0.0D);
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull MobTFTowerGolem entity,
		                                                     float brightness, float partialTick,
		                                                     int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model == null) {
				return null;
			}
			model.resetBones();

			float bodyYaw = this.getBodyYaw(entity, partialTick);
			BoneTransform head = model.getTransform("head");
			if (head != null) {
				head.rotX = this.getHeadPitch(entity, partialTick);
				head.rotY = this.getHeadYaw(entity, partialTick) - bodyYaw;
			}

			float limbSwing = this.getLimbSwing(entity, partialTick);
			float limbYaw = this.getLimbYaw(entity, partialTick);

			setRot(model, "rightLeg", -ARM_SWING * triangleWave(limbSwing, WALK_PERIOD) * limbYaw,
				0.0D, 0.0D);
			setRot(model, "leftLeg", ARM_SWING * triangleWave(limbSwing, WALK_PERIOD) * limbYaw,
				0.0D, 0.0D);

			int attackTimer = entity.getAttackTimer();
			float rightArm;
			float leftArm;
			if (attackTimer > 0) {

				float swing = ARM_ATTACK_REST
					+ ARM_SWING * triangleWave(attackTimer - partialTick, ATTACK_PERIOD);
				rightArm = swing;
				leftArm = swing;
			} else {
				rightArm = (ARM_REST + ARM_SWING * triangleWave(limbSwing, WALK_PERIOD)) * limbYaw;
				leftArm = (ARM_REST - ARM_SWING * triangleWave(limbSwing, WALK_PERIOD)) * limbYaw;
			}
			setRot(model, "rightArm", rightArm, 0.0D, 0.0D);
			setRot(model, "leftArm", leftArm, 0.0D, 0.0D);

			if (entity.walkAnimSpeed >= LURCH_THRESHOLD) {

				float roll = LURCH_DEGREES *
					triangleWave(limbSwing + LURCH_PHASE, LURCH_PERIOD) * MathHelper.DEG_TO_RAD;

				BoneTransform body = model.getTransform("body");
				if (body != null) {
					body.rotZ = roll;
				}
			}
			return model;
		}

		public static float triangleWave(float t, float period) {
			return (Math.abs(t % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
		}
	}

	public static final class Termite extends MobRenderer<MobTFTowerTermite> {

		private static final int SEGMENTS = 7;

		private static final int ANCHOR = 2;

		private static final float RATE = 0.9F;
		private static final float PHASE_PER_SEGMENT = 0.15F * (float) Math.PI;

		private static final float YAW_AMPLITUDE = (float) Math.PI * 0.05F;
		private static final float SLIDE_AMPLITUDE = (float) Math.PI * 0.2F;

		public Termite() {
			super(0.3F);
			setModel(MODEL_KEY, "geometry.towertermite", 0.0D);
		}

		@Nullable
		@Override
		protected StaticEntityModel getAndSetupModelForLayer(@NotNull MobTFTowerTermite entity,
		                                                     float brightness, float partialTick,
		                                                     int layer) {
			StaticEntityModel model = this.getModel(MODEL_KEY);
			if (model == null) {
				return null;
			}
			model.resetBones();

			float t = this.getLimbPitch(entity, partialTick);
			double[] yaw = new double[SEGMENTS];
			double[] slide = new double[SEGMENTS];

			for (int i = 0; i < SEGMENTS; i++) {
				float phase = t * RATE + i * PHASE_PER_SEGMENT;
				int reach = Math.abs(i - ANCHOR);
				yaw[i] = MathHelper.cos(phase) * YAW_AMPLITUDE * (1 + reach);
				slide[i] = MathHelper.sin(phase) * SLIDE_AMPLITUDE * reach;

				BoneTransform segment = model.getTransform("segment" + i);
				if (segment != null) {
					segment.rotY = yaw[i];
					segment.posX = slide[i];
				}
			}

			setPlate(model, "plate0", yaw[2], 0.0);
			setPlate(model, "plate1", yaw[4], slide[4]);
			setPlate(model, "plate2", yaw[1], slide[1]);
			return model;
		}

		private static void setPlate(StaticEntityModel model, String bone, double rotY, double posX) {
			BoneTransform t = model.getTransform(bone);
			if (t != null) {
				t.rotY = rotY;
				t.posX = posX;
			}
		}
	}
}
