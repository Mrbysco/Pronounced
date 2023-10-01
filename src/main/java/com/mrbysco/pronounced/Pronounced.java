package com.mrbysco.pronounced;

import com.mojang.logging.LogUtils;
import com.mrbysco.pronounced.client.ClientHandler;
import com.mrbysco.pronounced.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Pronounced.MOD_ID)
public class Pronounced {
	public static final String MOD_ID = "pronounced";
	public static final Logger LOGGER = LogUtils.getLogger();

	public Pronounced() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		eventBus.addListener(this::setup);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			eventBus.addListener(ClientHandler::registerKeybinds);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::loginEvent);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::logoutEvent);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onKeyInput);
		});
	}

	private void setup(final FMLCommonSetupEvent event) {
		PacketHandler.registerPackets();
	}
}
