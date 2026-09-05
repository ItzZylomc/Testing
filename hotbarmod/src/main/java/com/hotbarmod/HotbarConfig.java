package com.hotbarmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds every setting exposed by the /hotbarmod config screen and takes
 * care of persisting them to config/hotbarmod.json.
 */
public class HotbarConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("hotbarmod.json");

	public static HotbarConfig INSTANCE = load();

	/** Scale multiplier applied to the whole hotbar. 1.0 = vanilla size. */
	public float hotbarScale = 0.75f;

	/** Extra vertical offset (in pixels, pre-scale) from the vanilla position. Positive = lower. */
	public int verticalOffset = 0;

	/** Whether the offhand/attack-indicator icons scale down along with the hotbar. */
	public boolean scaleSideIcons = true;

	public static final float MIN_SCALE = 0.4f;
	public static final float MAX_SCALE = 1.0f;

	public static HotbarConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				HotbarConfig loaded = GSON.fromJson(reader, HotbarConfig.class);
				if (loaded != null) {
					loaded.clamp();
					return loaded;
				}
			} catch (IOException e) {
				HotbarMod.LOGGER.error("Failed to load hotbarmod.json, using defaults", e);
			}
		}
		return new HotbarConfig();
	}

	public void save() {
		clamp();
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			HotbarMod.LOGGER.error("Failed to save hotbarmod.json", e);
		}
	}

	private void clamp() {
		if (hotbarScale < MIN_SCALE) hotbarScale = MIN_SCALE;
		if (hotbarScale > MAX_SCALE) hotbarScale = MAX_SCALE;
	}
}
