package com.twilightforest.client.render;

import com.twilightforest.entity.MobTFLich;
import com.twilightforest.entity.MobTFRedcap;
import com.twilightforest.entity.MobTFSkeletonDruid;
import com.twilightforest.entity.MobTFWraith;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.util.helper.MathHelper;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public final class TFHumanoidRenderers {
	private TFHumanoidRenderers() {}

	public static final class Redcap extends MobRendererTFBiped<MobTFRedcap> {
		public Redcap() {
			super("geometry.redcap", 0.0D, 0.5F);
		}
	}

	public static final class SkeletonDruid extends MobRendererTFBiped<MobTFSkeletonDruid> {
		public SkeletonDruid() {
			super("geometry.skeletondruid", 0.0D, 0.5F);
		}
	}

	public static final class Wraith extends MobRendererTFBiped<MobTFWraith> {
		public Wraith() {
			super("geometry.wraith", 0.0D, 0.5F);
		}

		@Override
		protected void poseExtra(StaticEntityModel model, MobTFWraith entity, float limbSwing,
		                         float limbYaw, float partialTick) {
			BoneTransform right = model.getTransform("armRight");
			BoneTransform left = model.getTransform("armLeft");
			if (right == null || left == null) {
				return;
			}

			float ticks = this.getLimbPitch(entity, partialTick);
			float swing = entity.getSwingProgress(partialTick);
			float lunge = MathHelper.sin(swing * (float) Math.PI);
			float recover = MathHelper.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * (float) Math.PI);

			right.rotZ = 0.0;
			left.rotZ = 0.0;
			right.rotY = -(0.1F - lunge * 0.6F);
			left.rotY = 0.1F - lunge * 0.6F;

			right.rotX = -90.0F * MathHelper.DEG_TO_RAD;
			left.rotX = -90.0F * MathHelper.DEG_TO_RAD;
			right.rotX -= lunge * 1.2F - recover * 0.4F;
			left.rotX -= lunge * 1.2F - recover * 0.4F;

			right.rotZ = right.rotZ + (MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F);
			left.rotZ = left.rotZ - (MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F);
			right.rotX = right.rotX + MathHelper.sin(ticks * 0.067F) * 0.05F;
			left.rotX = left.rotX - MathHelper.sin(ticks * 0.067F) * 0.05F;
		}
	}

	public static final class Lich extends MobRendererTFBiped<MobTFLich> {
		public Lich() {
			super("geometry.lich", 0.0D, 1.0F);
		}

		@Override
		protected void poseExtra(StaticEntityModel model, MobTFLich entity, float limbSwing,
		                         float limbYaw, float partialTick) {
			BoneTransform right = model.getTransform("armRight");
			BoneTransform left = model.getTransform("armLeft");
			if (right == null || left == null) {
				return;
			}

			float ticks = this.getLimbPitch(entity, partialTick);
			float swing = entity.getSwingProgress(partialTick);
			float lunge = MathHelper.sin(swing * (float) Math.PI);
			float recover = MathHelper.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * (float) Math.PI);

			right.rotZ = 0.0;
			left.rotZ = 0.0;
			right.rotY = -(0.1F - lunge * 0.6F);
			left.rotY = 0.1F - lunge * 0.6F;

			right.rotX = -90.0F * MathHelper.DEG_TO_RAD;
			left.rotX = -90.0F * MathHelper.DEG_TO_RAD;
			right.rotX -= lunge * 1.2F - recover * 0.4F;
			left.rotX -= lunge * 1.2F - recover * 0.4F;

			right.rotZ = right.rotZ + (MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F);
			left.rotZ = left.rotZ - (MathHelper.cos(ticks * 0.09F) * 0.05F + 0.05F);
			right.rotX = right.rotX + MathHelper.sin(ticks * 0.067F) * 0.05F;
			left.rotX = left.rotX - MathHelper.sin(ticks * 0.067F) * 0.05F;
		}
	}
}
