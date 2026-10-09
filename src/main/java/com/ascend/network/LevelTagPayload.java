package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

/** Server to client: a player's level, shown above their head (0 hides it). */
public record LevelTagPayload(UUID player, int level) implements CustomPayload {
	public static final CustomPayload.Id<LevelTagPayload> ID = new CustomPayload.Id<>(Ascend.id("level_tag"));
	public static final PacketCodec<ByteBuf, LevelTagPayload> CODEC = PacketCodec.tuple(
			Uuids.PACKET_CODEC, LevelTagPayload::player,
			PacketCodecs.VAR_INT, LevelTagPayload::level,
			LevelTagPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
