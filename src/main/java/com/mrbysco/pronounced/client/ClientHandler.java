package com.mrbysco.pronounced.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.pronounced.client.speech.SphinxThread;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ClientHandler {
	public static KeyMapping KEY_TOGGLE = new KeyMapping(
			"key.pronounced.voice",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_KP_ADD,
			"category.pronounced.main");

	public static void registerKeybinds(final RegisterKeyMappingsEvent event) {
		event.register(KEY_TOGGLE);
	}

	private static boolean ingame = false;

	public static void loginEvent(LoggingIn event) {
		SphinxThread sphinxThread = SphinxThread.getInstance();

		Thread thread = new Thread(sphinxThread);
		thread.start();
		ingame = true;
	}

	public static void logoutEvent(ClientPlayerNetworkEvent.LoggingOut event) {
		if (ingame)
			SphinxThread.getInstance().pauseRecognition();
	}

	private static boolean recording = false;

	public static void onKeyInput(InputEvent.Key event) {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen != null && event.getAction() != GLFW.GLFW_PRESS)

			if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
				return;
			}

		if (KEY_TOGGLE.consumeClick()) {
			recording = !recording;
			if (recording) {
				SphinxThread.getInstance().resumeRecognition(Minecraft.getInstance().player);
			} else {
				SphinxThread.getInstance().pauseRecognition();
			}
		}
	}
}
