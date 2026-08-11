package com.twilightforest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public abstract class MobRendererTFBiped<T extends Mob> extends MobRenderer<T> {

	protected static final String MODEL_KEY = "main";

	protected MobRendererTFBiped(String modelId, double inflation, float shadowSize) {
		super(shadowSize);
		setModel(MODEL_KEY, modelId, inflation);
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

		setRotation(model, "head", headPitch * MathHelper.DEG_TO_RAD, headYaw * MathHelper.DEG_TO_RAD);
		setRotation(model, "headwear", headPitch * MathHelper.DEG_TO_RAD, headYaw * MathHelper.DEG_TO_RAD);
		setRotation(model, "cap", headPitch * MathHelper.DEG_TO_RAD, headYaw * MathHelper.DEG_TO_RAD);

		float limbSwing = this.getLimbSwing(entity, partialTick);
		float limbYaw = this.getLimbYaw(entity, partialTick);

		setRotX(model, "legRight", MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbYaw);
		setRotX(model, "legLeft", MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbYaw);

		float armRight = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbYaw * 0.5F;
		float armLeft = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbYaw * 0.5F;

		if (entity instanceof IItemHolding holder && holder.getHeldItem() != null) {
			if (holder.isLeftHanded()) {
				armLeft = armLeft * 0.5F - 18.0F * MathHelper.DEG_TO_RAD;
			} else {
				armRight = armRight * 0.5F - 18.0F * MathHelper.DEG_TO_RAD;
			}
		}

		setRotX(model, "armRight", armRight);
		setRotX(model, "armLeft", armLeft);

		this.poseExtra(model, entity, limbSwing, limbYaw, partialTick);
		return model;
	}

	protected static void setRotX(StaticEntityModel model, String bone, float angle) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX = angle;
		}
	}

	protected static void setRotation(StaticEntityModel model, String bone, float pitch, float yaw) {
		BoneTransform t = model.getTransform(bone);
		if (t != null) {
			t.rotX = pitch;
			t.rotY = yaw;
		}
	}
}
