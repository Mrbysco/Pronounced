package com.mrbysco.pronounced.network;

import com.mrbysco.pronounced.Pronounced;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Pronounced.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public static int i = 0;

	public static void registerPackets() {
		PacketHandler.CHANNEL.registerMessage(i++, CastSpellMessage.class, CastSpellMessage::encode, CastSpellMessage::decode, CastSpellMessage::handle);
	}
}
