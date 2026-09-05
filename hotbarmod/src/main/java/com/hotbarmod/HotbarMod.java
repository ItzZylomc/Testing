package com.hotbarmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotbarMod implements ClientModInitializer {

	public static final String MOD_ID = "hotbarmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// Make sure the config is loaded (and the file created) as soon as the game starts.
		HotbarConfig.INSTANCE = HotbarConfig.load();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("hotbarmod")
						.executes(context -> {
							MinecraftClient client = MinecraftClient.getInstance();
							// setScreen must run on the render thread; client.execute()
							// makes sure of that even if command parsing ever moves off it.
							client.execute(() -> client.setScreen(new HotbarConfigScreen(null)));
							return 1;
						})
						// Also accept /hotbarmod config as an alias, in case people expect a subcommand.
						.then(ClientCommandManager.literal("config")
								.executes(context -> {
									MinecraftClient client = MinecraftClient.getInstance();
									client.execute(() -> client.setScreen(new HotbarConfigScreen(null)));
									return 1;
								}))
				));

		LOGGER.info("Hotbar Mod initialized - type /hotbarmod in chat to configure it.");
	}
}
