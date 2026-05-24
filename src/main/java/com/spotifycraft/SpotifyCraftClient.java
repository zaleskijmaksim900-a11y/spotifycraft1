package com.spotifycraft;

import com.spotifycraft.client.gui.SpotifyScreen;
import com.spotifycraft.music.MusicManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class SpotifyCraftClient implements ClientModInitializer {

    public static final String MOD_ID = "spotifycraft";
    public static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding: M to open music player
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.spotifycraft.open",
            GLFW.GLFW_KEY_M,
            "category.spotifycraft"
        ));

        // Initialize music manager (creates folder, scans files)
        MusicManager.getInstance().initialize();

        // Listen for key press each tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.consumeClick()) {
                client.setScreen(new SpotifyScreen(client.screen));
            }
        });

        System.out.println("[SpotifyCraft] Mod loaded! Press M to open music player.");
    }
}
