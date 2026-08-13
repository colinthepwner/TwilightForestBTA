package com.twilightforest.client.sound;

import com.twilightforest.TwilightForest;
import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundCategoryHelper;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.sound.SoundCategory;

import java.lang.reflect.Method;
import java.net.URL;

@Environment(EnvType.CLIENT)
public final class TFAmbience {
	private TFAmbience() {}

	private static final String BED = TwilightForest.MOD_ID + ":ambient.tf.night_bed";
	private static final String VOICE = TwilightForest.MOD_ID + ":ambient.tf.night_voice";

	private static final String BED_SOURCE = TwilightForest.MOD_ID + ":ambience.bed";
	private static final String VOICE_SOURCE = TwilightForest.MOD_ID + ":ambience.voice";

	private static final float BED_VOLUME = 0.22F;
	private static final float VOICE_VOLUME = 0.80F;

	private static final int ATTENUATION_NONE = 0;

	private static boolean playing;
	private static boolean reflectionFailed;
	private static Method getSoundSystem;
	private static Method newStreamingSource;
	private static Method setVolume;
	private static Method play;
	private static Method stop;

	public static void tick(Minecraft mc) {
		boolean inTwilight = mc != null
			&& mc.currentWorld != null
			&& mc.thePlayer != null
			&& mc.currentWorld.getWorldType() == WorldTypeTwilightForest.TWILIGHT_FOREST;

		if (inTwilight && !playing) {
			start();
		} else if (!inTwilight && playing) {
			stop();
		}
	}

	private static void start() {
		if (loop(BED, BED_SOURCE, BED_VOLUME) && loop(VOICE, VOICE_SOURCE, VOICE_VOLUME)) {
			playing = true;
		} else {

			stop();
		}
	}

	private static boolean loop(String soundName, String sourceName, float volume) {
		Object soundSystem = resolve();
		if (soundSystem == null) {
			return false;
		}

		SoundEntry entry = SoundRepository.SOUNDS.getSoundEntry(soundName);
		if (entry == null) {
			return false;
		}

		URL url = entry.getURL();
		if (url == null) {
			return false;
		}

		try {

			newStreamingSource.invoke(soundSystem, false, sourceName, url, entry.name, true,
				0.0F, 0.0F, 0.0F, ATTENUATION_NONE, 0.0F);

			setVolume.invoke(soundSystem, sourceName,
				volume * SoundCategoryHelper.getEffectiveVolume(SoundCategory.WORLD_SOUNDS) * entry.volume);
			play.invoke(soundSystem, sourceName);
			return true;
		} catch (ReflectiveOperationException e) {
			TwilightForest.LOGGER.error("Could not start ambience layer '{}'.", soundName, e);
			reflectionFailed = true;
			return false;
		}
	}

	private static void stop() {
		Object soundSystem = resolve();
		if (soundSystem != null) {
			try {
				stop.invoke(soundSystem, BED_SOURCE);
				stop.invoke(soundSystem, VOICE_SOURCE);
			} catch (ReflectiveOperationException e) {
				TwilightForest.LOGGER.error("Could not stop the Twilight Forest ambience.", e);
				reflectionFailed = true;
			}
		}
		playing = false;
	}

	private static Object resolve() {
		if (reflectionFailed) {
			return null;
		}

		try {

			if (getSoundSystem == null) {
				getSoundSystem = SoundEngine.class.getMethod("getSoundSystem");
			}

			Object soundSystem = getSoundSystem.invoke(null);
			if (soundSystem == null) {

				return null;
			}

			if (newStreamingSource != null) {
				return soundSystem;
			}

			Class<?> type = soundSystem.getClass();
			newStreamingSource = type.getMethod("newStreamingSource", boolean.class, String.class,
				URL.class, String.class, boolean.class, float.class, float.class, float.class,
				int.class, float.class);
			setVolume = type.getMethod("setVolume", String.class, float.class);
			play = type.getMethod("play", String.class);
			stop = type.getMethod("stop", String.class);
			return soundSystem;
		} catch (ReflectiveOperationException e) {
			TwilightForest.LOGGER.error(
				"paulscode SoundSystem does not look the way this mod expects; ambience disabled.", e);
			reflectionFailed = true;
			return null;
		}
	}
}
