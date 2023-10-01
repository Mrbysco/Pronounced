package com.mrbysco.pronounced.network;

import com.mrbysco.spelled.util.SpellUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.UUID;
import java.util.function.Supplier;

public class CastSpellMessage {
	private final String spell;
	private final UUID uuid;

	public CastSpellMessage(String spell, UUID uuid) {
		this.spell = spell;
		this.uuid = uuid;
	}

	private CastSpellMessage(FriendlyByteBuf buf) {
		this.spell = buf.readUtf();
		this.uuid = buf.readUUID();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(this.spell);
		buf.writeUUID(this.uuid);
	}

	public static CastSpellMessage decode(final FriendlyByteBuf packetBuffer) {
		return new CastSpellMessage(packetBuffer);
	}

	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer()) {
				ServerPlayer player = ctx.getSender();
				if (player.getUUID().equals(this.uuid)) {
					Component component = Component.translatable("chat.type.text", player.getDisplayName(),
							net.minecraftforge.common.ForgeHooks.newChatWithLinks(this.spell));
//					SpellUtil.manualCastSpell(player, this.spell, component);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}