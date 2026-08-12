package com.twilightforest.achievement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.achievements.ScreenAchievements;
import net.minecraft.client.gui.achievements.data.AchievementPage;
import net.minecraft.client.gui.achievements.data.AchievementPages;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class TFAchievementPage extends AchievementPage {

	private static boolean registered;

	public static void register() {
		if (registered || TFAchievements.ARRIVAL == null) {
			return;
		}
		registered = true;
		AchievementPages.register(new TFAchievementPage());
	}

	private TFAchievementPage() {

		addAchievement(TFAchievements.PORTAL, -2, 1);
		addAchievement(TFAchievements.ARRIVAL, 0, 0);
		addAchievement(TFAchievements.HUNTER, 2, 2);
		addAchievement(TFAchievements.NAGA, 2, 4);
		addAchievement(TFAchievements.LICH, 4, 3);
		addAchievement(TFAchievements.HILL_1, -2, -1);
		addAchievement(TFAchievements.HILL_3, -1, -3);
		addAchievement(TFAchievements.HEDGE, 2, -3);
	}

	@Override
	public String getName() {
		return I18n.getInstance().translateKey("achievement.page.twilightforest");
	}

	@Override
	public String getDescription() {
		return I18n.getInstance().translateKey("achievement.page.twilightforest.desc");
	}

	@Override
	public AchievementEntry onOpenAchievement() {
		return getEntry(TFAchievements.ARRIVAL);
	}

	@Override
	public IconCoordinate getBackgroundTile(ScreenAchievements screen, int layer, Random random,
	                                        int x, int y) {
		int roll = random.nextInt(1 + layer) + layer / 2;
		if (roll <= 37) {
			return getTextureFromBlock(Blocks.GRASS);
		}
		if (roll <= 45) {
			return getTextureFromBlock(Blocks.LOG_OAK);
		}
		return getTextureFromBlock(Blocks.LEAVES_OAK);
	}

	@Override
	public void postProcessBackground(ScreenAchievements screen, Random random,
	                                  ScreenAchievements.BGLayer layer, int x, int y) {

	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(com.twilightforest.block.TFBlocks.LOG_TWILIGHT_OAK);
	}

	@Override
	public int backgroundLayers() {
		return 1;
	}

	@Override
	public int backgroundColor() {
		return 0x1B2A1B;
	}

	@Override
	public IconCoordinate getAchievementIcon(Achievement achievement) {
		return null;
	}

	@Override
	public int lineColorLocked(boolean bright) {
		return bright ? 0x4C664C : 0x233323;
	}

	@Override
	public int lineColorUnlocked(boolean bright) {
		return bright ? 0x88CC88 : 0x448844;
	}

	@Override
	public int lineColorCanUnlock(boolean bright) {
		return bright ? 0xCCCC88 : 0x888844;
	}
}
