package com.twilightforest.asset;

import com.twilightforest.TwilightForest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public final class TFBlockTextureBridge {
	private TFBlockTextureBridge() {}

	private static final String[] MANIFESTS = {
		"/assets/twilightforest/block-bridge.properties",
		"/assets/twilightforest/item-bridge.properties",
	};

	private static final int TILES_ACROSS = 16;

	public static int slicedCount = -1;
	public static List<String> problems = new ArrayList<>();

	public static final Set<String> writtenTextureIds = new HashSet<>();

	private static boolean scanned = false;

	public static void ensureScanned() {
		if (scanned) {
			return;
		}
		File gameDir;
		try {
			gameDir = net.minecraft.client.Minecraft.getMinecraft().getMinecraftDir();
		} catch (Throwable t) {
			return;
		}
		if (gameDir == null) {
			return;
		}
		File packDir = new File(gameDir, "texturepacks/" + TFAssetBridge.PACK_NAME);
		if (!packDir.isDirectory()) {
			return;
		}
		scanned = true;
		recordExisting(packDir);
	}

	private static void recordWritten(String packPath) {
		String id = textureIdFor(packPath);
		if (id != null) writtenTextureIds.add(id);
	}

	public static void recordExisting(File packDir) {
		for (Slice slice : manifest()) {
			if (new File(packDir, slice.path).isFile()) {
				recordWritten(slice.path);
			}
		}
	}

	private static String textureIdFor(String packPath) {
		String prefix = "assets/";
		String middle = "/textures/";
		if (!packPath.startsWith(prefix) || !packPath.endsWith(".png")) return null;
		int mid = packPath.indexOf(middle);
		if (mid < 0) return null;
		String namespace = packPath.substring(prefix.length(), mid);
		String rest = packPath.substring(mid + middle.length(), packPath.length() - ".png".length());
		return namespace + ":" + rest;
	}

	private record Slice(String sheet, int index, String path) {}

	public static List<String> wantedEntries() {
		List<String> wanted = new ArrayList<>();
		for (Slice slice : manifest()) {
			if (!wanted.contains(slice.sheet)) wanted.add(slice.sheet);
		}
		return wanted;
	}

	private static List<Slice> manifest() {
		List<Slice> slices = new ArrayList<>();
		for (String manifest : MANIFESTS) {
			readManifest(manifest, slices);
		}
		return slices;
	}

	private static void readManifest(String resource, List<Slice> into) {
		Properties props = new Properties();
		try (InputStream in = TFBlockTextureBridge.class.getResourceAsStream(resource)) {
			if (in == null) return;
			props.load(in);
		} catch (IOException e) {
			return;
		}

		for (String key : new TreeSet<>(props.stringPropertyNames())) {
			int hash = key.indexOf('#');
			if (hash < 0) continue;
			String sheet = key.substring(0, hash).trim().toLowerCase(Locale.ROOT);
			int index;
			try {
				index = Integer.parseInt(key.substring(hash + 1).trim());
			} catch (NumberFormatException e) {
				continue;
			}
			String path = props.getProperty(key).trim();
			if (!path.isEmpty()) into.add(new Slice(sheet, index, path));
		}
	}

	public static int run(Map<String, byte[]> archive, File packDir) {
		List<Slice> slices = manifest();
		problems = new ArrayList<>();
		if (slices.isEmpty()) {
			problems.add("the sheet manifests " + String.join(" and ", MANIFESTS)
				+ " are empty or missing");
			slicedCount = 0;
			return 0;
		}

		Map<String, BufferedImage> sheets = new LinkedHashMap<>();
		int written = 0;

		for (Slice slice : slices) {
			BufferedImage sheet = sheets.get(slice.sheet);
			if (sheet == null) {
				if (sheets.containsKey(slice.sheet)) continue;
				byte[] bytes = archive.get(slice.sheet);
				if (bytes == null) {
					sheets.put(slice.sheet, null);
					problems.add("the archive has no " + slice.sheet
						+ ", so the blocks it supplies will fall back to their vanilla look");
					continue;
				}
				try {
					sheet = ImageIO.read(new ByteArrayInputStream(bytes));
				} catch (IOException e) {
					sheet = null;
				}
				if (sheet == null) {
					sheets.put(slice.sheet, null);
					problems.add(slice.sheet + " could not be decoded as an image");
					continue;
				}
				sheets.put(slice.sheet, sheet);
			}

			int tile = sheet.getWidth() / TILES_ACROSS;
			if (tile <= 0) {
				problems.add(slice.sheet + " is only " + sheet.getWidth() + " pixels wide, which is not a "
					+ TILES_ACROSS + "-tile sheet");
				continue;
			}

			int x = (slice.index & 15) * tile;
			int y = (slice.index >> 4) * tile;
			if (x + tile > sheet.getWidth() || y + tile > sheet.getHeight()) {
				problems.add("tile " + slice.index + " is outside " + slice.sheet);
				continue;
			}

			try {

				BufferedImage out = new BufferedImage(tile, tile, BufferedImage.TYPE_INT_ARGB);
				out.createGraphics().drawImage(
					sheet.getSubimage(x, y, tile, tile), 0, 0, null);

				File target = new File(packDir, slice.path);
				File parent = target.getParentFile();
				if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
					problems.add("could not create " + parent.getPath());
					continue;
				}
				ImageIO.write(out, "png", target);
				written++;
				recordWritten(slice.path);
			} catch (IOException | RuntimeException e) {
				problems.add("could not write " + slice.path + " (" + e + ")");
			}
		}

		slicedCount = written;
		if (written > 0) {
			TwilightForest.LOGGER.info("Block texture bridge: {} tiles cut out of the original's sprite sheets",
				written);
		}
		for (String problem : problems) {
			TwilightForest.LOGGER.warn("Block texture bridge: {}", problem);
		}
		return written;
	}
}
