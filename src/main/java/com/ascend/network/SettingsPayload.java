package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Server to client: the world settings the client needs (milestones for the double jump, mob level tag distance). */
public record SettingsPayload(boolean milestones, int levelTagDistance) implements CustomPayload {
	public static final CustomPayload.Id<SettingsPayload> ID = new CustomPayload.Id<>(Ascend.id("settings"));
	public static final PacketCodec<ByteBuf, SettingsPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL, SettingsPayload::milestones,
			PacketCodecs.VAR_INT, SettingsPayload::levelTagDistance,
			SettingsPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
