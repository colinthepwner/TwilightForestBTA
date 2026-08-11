package com.twilightforest.mixin;

import com.twilightforest.TFConfig;
import com.twilightforest.TwilightForest;
import com.twilightforest.world.TFDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MushroomPortalMixin {

	private int twilightforest$portalCooldown = 0;

	private boolean twilightforest$wasHolding = false;

	@Inject(method = "runTick", at = @At("TAIL"))
	private void twilightforest$mushroomPortal(CallbackInfo ci) {
		if (!TFConfig.MUSHROOM_PORTAL || !TFDimension.isRegistered()) {
			return;
		}

		Minecraft mc = (Minecraft) (Object) this;
		Player player = mc.thePlayer;
		if (player == null || mc.currentWorld == null) {
			this.twilightforest$wasHolding = false;
			return;
		}

		ItemStack held = player.inventory.getCurrentItem();
		boolean holding = held != null && held.itemID == TFConfig.MUSHROOM_PORTAL_ITEM_ID;

		boolean rising = holding && !this.twilightforest$wasHolding;
		this.twilightforest$wasHolding = holding;

		if (this.twilightforest$portalCooldown > 0) {
			this.twilightforest$portalCooldown--;
			return;
		}

		if (!rising) {
			return;
		}

		int twilightId = TFDimension.getDimensionId();
		int here = mc.currentWorld.dimension.id;

		if (here == twilightId) {
			this.twilightforest$portalCooldown = 60;
			mc.usePortal(0, null);
			TwilightForest.LOGGER.info("Mushroom portal: leaving the Twilight Forest.");
			return;
		}

		if (here != 0) {
			return;
		}

		this.twilightforest$portalCooldown = 60;
		mc.usePortal(twilightId, null);
		TwilightForest.LOGGER.info("Mushroom portal: entering the Twilight Forest (dimension {}).",
			twilightId);
	}
}
