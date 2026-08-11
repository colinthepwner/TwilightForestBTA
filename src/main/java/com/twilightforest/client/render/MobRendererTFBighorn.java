package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFBighorn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.util.helper.Colors;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public class MobRendererTFBighorn extends MobRendererTFQuadruped<MobTFBighorn> {

	private static final String FUR_KEY = "fur";

	public MobRendererTFBighorn() {
		super("geometry.bighorn", 0.0D, 0.7F);

		setModel(FUR_KEY, "geometry.bighorn_fur", 0.0D);
	}

	@Override
	protected int maxRenderLayer(@NotNull MobTFBighorn entity) {
		return 1;
	}

	@Nullable
	@Override
	protected StaticEntityModel getAndSetupModelForLayer(@NotNull MobTFBighorn entity, float brightness,
	                                                     float partialTick, int layer) {
		StaticEntityModel model = this.getModel(layer == 1 ? FUR_KEY : MODEL_KEY);
		if (model == null) {
			return null;
		}

		if (layer == 1) {

			Color color = Colors.allSheepColors[entity.getFleeceColor().blockMeta];
			GLRenderer.setColor3f(
				brightness * color.getRed() / 255.0F,
				brightness * color.getGreen() / 255.0F,
				brightness * color.getBlue() / 255.0F);
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

		return model;
	}
}
