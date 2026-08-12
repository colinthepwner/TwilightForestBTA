package com.twilightforest;

import net.minecraft.core.world.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static com.twilightforest.TwilightForest.MOD_ID;

@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008"})
public final class TFConfig {
	private TFConfig() {}

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static TomlConfigHandler cfg;

	public static final String GENERAL_CATEGORY = "General";

	public static final int DIMENSION_AUTO = -1;

	public static int DIMENSION_ID = DIMENSION_AUTO;

	@SuppressWarnings({"java:S899", "ResultOfMethodCallIgnored"})
	static void init() {
		LOGGER.info("Initializing config..");

		Toml props = new Toml("Twilight Forest.toml");
		assembleProperties(props);

		cfg = new TomlConfigHandler(MOD_ID, props);

		if (cfg.getConfigFile().exists()) {
			cfg.loadConfig();
		} else {
			try {
				cfg.getConfigFile().createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			cfg.writeConfig();
		}

		loadProperties();
	}

	private static void loadProperties() {
		DIMENSION_ID = cfgGetValueOrDefault(GENERAL_CATEGORY + ".DIMENSION_ID", DIMENSION_ID);
	}

	public static int resolveDimensionId() {
		int nextFree = Dimension.getDimensionList().size();

		if (DIMENSION_ID == DIMENSION_AUTO) {
			LOGGER.info("Allocating dimension id {} (the next free contiguous slot).", nextFree);
			persistDimensionId(nextFree);
			return nextFree;
		}

		if (DIMENSION_ID == nextFree) {
			LOGGER.info("Claiming dimension id {} as recorded in the config.", DIMENSION_ID);
			return DIMENSION_ID;
		}

		if (Dimension.getDimensionList().containsKey(DIMENSION_ID)) {
			LOGGER.error(
				"Dimension id {} is recorded in the config but is already registered by something else. "
					+ "The Twilight Forest will not be reachable this session. Existing Twilight Forest "
					+ "worlds still reference id {}, so this is reported rather than silently moved to {} "
					+ "-- resolve the clash by changing the other mod's id, or set DIMENSION_ID = -1 to "
					+ "re-allocate and accept that existing Twilight Forest worlds become unreachable.",
				DIMENSION_ID, DIMENSION_ID, nextFree);
			return -1;
		}

		LOGGER.error(
			"Config DIMENSION_ID = {} would leave a gap in the dimension id space (next free is {}). "
				+ "BTA's WorldTypeGroups.Group walks ids 0..size-1 and requires each one to exist, so a "
				+ "gap is an NPE during world creation. Using {} instead.",
			DIMENSION_ID, nextFree, nextFree);
		persistDimensionId(nextFree);
		return nextFree;
	}

	private static void persistDimensionId(int id) {
		DIMENSION_ID = id;

		File file = cfg == null ? null : cfg.getConfigFile();
		if (file == null || !file.isFile()) {
			LOGGER.warn("No config file to record the resolved dimension id in; it will be "
				+ "re-allocated on the next launch.");
			return;
		}

		try {
			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			boolean replaced = false;

			for (int i = 0; i < lines.size(); i++) {
				String trimmed = lines.get(i).trim();
				if (!trimmed.startsWith("DIMENSION_ID")) {
					continue;
				}

				String indent = lines.get(i).substring(0, lines.get(i).indexOf("DIMENSION_ID"));
				lines.set(i, indent + "DIMENSION_ID = " + id);
				replaced = true;
				break;
			}

			if (!replaced) {
				LOGGER.warn("Config file has no DIMENSION_ID line to update; the dimension id will "
					+ "be re-allocated on the next launch.");
				return;
			}

			Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
			LOGGER.info("Recorded dimension id {} in the config.", id);
		} catch (IOException | RuntimeException e) {

			LOGGER.warn("Could not write the resolved dimension id back to the config.", e);
		}
	}

	private static void assembleProperties(Toml properties) {
		properties.addCategory(GENERAL_CATEGORY)
			.addEntry("cfgVersion", 1)
			.addEntry("DIMENSION_ID", DIMENSION_ID);
	}

	@SuppressWarnings("unchecked")
	static <T> T cfgGetValueOrDefault(String key, T def) {
		T res = null;

		try {
			if (def instanceof String) {
				res = (T) cfg.getString(key);
			} else if (def instanceof Integer) {
				res = (T) Integer.valueOf(cfg.getInt(key));
			} else if (def instanceof Long) {
				res = (T) Long.valueOf(cfg.getLong(key));
			} else if (def instanceof Boolean) {
				res = (T) Boolean.valueOf(cfg.getBoolean(key));
			} else if (def instanceof Double || def instanceof Float) {
				double raw = cfg.getDouble(key);
				if (def instanceof Float) {
					res = (T) Float.valueOf((float) raw);
				} else {
					res = (T) Double.valueOf(raw);
				}
			} else {
				throw new RuntimeException("Invalid value type!");
			}
		} catch (NullPointerException ignored) {  }

		if (res == null) {
			LOGGER.warn("Failed to load \"{}\"! Assuming default...", key);
			return def;
		}

		return res;
	}
}
