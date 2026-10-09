package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Server to client: the player's current level, XP, points and stats. */
public record SyncPayload(int level, int xp, int points, List<Integer> stats) implements CustomPayload {
	public static final CustomPayload.Id<SyncPayload> ID = new CustomPayload.Id<>(Ascend.id("sync"));
	public static final PacketCodec<ByteBuf, SyncPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, SyncPayload::level,
			PacketCodecs.VAR_INT, SyncPayload::xp,
			PacketCodecs.VAR_INT, SyncPayload::points,
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), SyncPayload::stats,
			SyncPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
