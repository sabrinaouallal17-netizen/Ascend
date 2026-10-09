package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Client to server: spend {@code amount} skill points in a stat (by its index in {@code Stat.values()}). */
public record SpendPayload(int stat, int amount) implements CustomPayload {
	public static final CustomPayload.Id<SpendPayload> ID = new CustomPayload.Id<>(Ascend.id("spend"));
	public static final PacketCodec<ByteBuf, SpendPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, SpendPayload::stat,
			PacketCodecs.VAR_INT, SpendPayload::amount,
			SpendPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
