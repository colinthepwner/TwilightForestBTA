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
public abstract class MobRendererTFQuadruped<T extends Mob> extends MobRenderer<T> {

	protected static final String MODEL_KEY = "main";

	protected MobRendererTFQuadruped(String modelId, double inflation, float shadowSize) {
		super(shadowSize);
		setModel(MODEL_KEY, modelId, inflation);
	}

	protected float limbSwingAmplitude(T entity, float partialTick) {
		return 1.4F;
	}

	protected void poseExtra(StaticEntityModel model, T entity, float limbSwing, float limbYaw,
	                         float partialTick) {

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
		float headYaw = this.getHeadYaw(entity, partialTick) - bodyYaw;
		float headPitch = this.getHeadPitch(entity, partialTick);

		BoneTransform head = model.getTransform("head");
		if (head != null) {
			head.rotX = headPitch * MathHelper.DEG_TO_RAD;
			head.rotY = headYaw * MathHelper.DEG_TO_RAD;
		}

		float limbSwing = this.getLimbSwing(entity, partialTick);
		float limbYaw = this.getLimbYaw(entity, partialTick);
		float amplitude = this.limbSwingAmplitude(entity, partialTick);

		setLegAngle(model, "leg0", MathHelper.cos(limbSwing * 0.6662F) * amplitude * limbYaw);
		setLegAngle(model, "leg1", MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * amplitude * limbYaw);
		setLegAngle(model, "leg2", MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * amplitude * limbYaw);
		setLegAngle(model, "leg3", MathHelper.cos(limbSwing * 0.6662F) * amplitude * limbYaw);

		this.poseExtra(model, entity, limbSwing, limbYaw, partialTick);
		return model;
	}

	protected static void setLegAngle(StaticEntityModel model, String bone, float angle) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX = angle;
		}
	}
}
