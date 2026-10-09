package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Server to client: the world settings the client needs (milestones, for the double jump). */
public record SettingsPayload(boolean milestones) implements CustomPayload {
	public static final CustomPayload.Id<SettingsPayload> ID = new CustomPayload.Id<>(Ascend.id("settings"));
	public static final PacketCodec<ByteBuf, SettingsPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL, SettingsPayload::milestones,
			SettingsPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
