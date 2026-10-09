package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Server to client: every counter (in {@code Counter} order) and the unlocked achievements (ordinals). */
public record QuestPayload(List<Integer> counters, List<Integer> achievements) implements CustomPayload {
	public static final CustomPayload.Id<QuestPayload> ID = new CustomPayload.Id<>(Ascend.id("quests"));
	public static final PacketCodec<ByteBuf, QuestPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), QuestPayload::counters,
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), QuestPayload::achievements,
			QuestPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
