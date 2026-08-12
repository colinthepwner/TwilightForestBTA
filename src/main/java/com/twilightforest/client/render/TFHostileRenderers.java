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
			return model;
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
