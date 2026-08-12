package com.twilightforest.client.render;

import com.twilightforest.TwilightForest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.core.block.entity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.useless.dragonfly.data.entity.mojang.EntityGeometryMojangData;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
public abstract class TileEntityRendererTFCritter<T extends TileEntity> extends TileEntityRenderer<T> {

	private final String geometryId;

	private final String texturePath;

	private StaticEntityModel model;
	private boolean resolved;

	protected TileEntityRendererTFCritter(String geometryId, String texturePath) {
		this.geometryId = geometryId;
		this.texturePath = texturePath;
	}

	@Nullable
	private StaticEntityModel model() {
		if (this.resolved) {
			return this.model;
		}
		this.resolved = true;
		if (EntityGeometryMojangData.Cache.getGeometry(this.geometryId) == null) {
			TwilightForest.LOGGER.info(
				"No bridged geometry for {}; its tile entity will not be drawn and the block's flat "
					+ "sprite is all you will see. This is expected without a copy of the original.",
				this.geometryId);
			return null;
		}
		this.model = EntityGeometryMojangData.Cache.getModel(this.geometryId, 0.0);
		return this.model;
	}

	private static int orientation(TileEntity tileEntity) {
		return tileEntity.getBlockMeta() & 7;
	}

	@Override
	public void doRender(@NotNull TessellatorGeneral tessellator, @NotNull T tileEntity,
	                     double x, double y, double z, float partialTick) {
		StaticEntityModel entityModel = this.model();
		if (entityModel == null) {
			return;
		}

		float rotX = 90.0F;
		float rotZ = 0.0F;
		switch (orientation(tileEntity)) {
			case 1 -> rotZ = -90.0F;
			case 2 -> rotZ = 90.0F;
			case 4 -> rotZ = 180.0F;
			case 5 -> rotX = 0.0F;
			case 6 -> rotX = 180.0F;
			default -> {  }
		}

		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindTexture(this.texturePath);
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GLRenderer.modelM4f().rotateX(Math.toRadians(rotX));
		GLRenderer.modelM4f().rotateZ(Math.toRadians(rotZ));
		GLRenderer.modelM4f().rotateY(Math.toRadians(this.currentYaw(tileEntity)));

		GLRenderer.modelM4f().scale(0.0625F, 0.0625F, -0.0625F);

		GLRenderer.modelM4f().translate(0.0F, -JAVA_ORIGIN_Y, 0.0F);
		GLRenderer.disableState(State.CULL_FACE);

		entityModel.resetBones();
		this.renderCritter(tessellator, entityModel, tileEntity, partialTick);

		GLRenderer.enableState(State.CULL_FACE);
		GLRenderer.popFrame();
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static final float JAVA_ORIGIN_Y = 24.0F;

	protected abstract float currentYaw(T tileEntity);

	protected abstract void renderCritter(TessellatorGeneral tessellator, StaticEntityModel model,
	                                      T tileEntity, float partialTick);
}
