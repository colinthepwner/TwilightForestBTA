package com.twilightforest.mixin;

import com.twilightforest.achievement.TFAchievements;
import com.twilightforest.item.TFItems;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.slot.SlotResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotResult.class)
public class NagaArmorerMixin {

	@Shadow
	private Player thePlayer;

	@Inject(method = "onTake", at = @At("TAIL"))
	private void twilightforest$nagaArmorer(ItemStack taken, CallbackInfo ci) {
		if (taken == null || thePlayer == null || thePlayer.world == null) {
			return;
		}

		if (thePlayer.world.isClientSide) {
			return;
		}

		if (!isNagaArmour(taken.getItem())) {
			return;
		}
		if (hasPiece(TFItems.NAGA_SCALE_TUNIC) && hasPiece(TFItems.NAGA_SCALE_LEGGINGS)) {
			TFAchievements.award(thePlayer, TFAchievements.NAGA_ARMOR);
		}
	}

	private boolean isNagaArmour(Item item) {
		return item != null
			&& (item == TFItems.NAGA_SCALE_TUNIC || item == TFItems.NAGA_SCALE_LEGGINGS);
	}

	private boolean hasPiece(Item piece) {
		if (piece == null) {

			return false;
		}
		ContainerInventory inventory = thePlayer.inventory;
		int size = inventory.getContainerSize();

		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack != null && stack.getItem() == piece) {
				return true;
			}
		}
		return false;
	}
}
