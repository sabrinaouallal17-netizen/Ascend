package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Client to server: the player double jumped (the server resets their fall distance). */
public record DoubleJumpPayload() implements CustomPayload {
	public static final DoubleJumpPayload INSTANCE = new DoubleJumpPayload();
	public static final CustomPayload.Id<DoubleJumpPayload> ID = new CustomPayload.Id<>(Ascend.id("double_jump"));
	public static final PacketCodec<ByteBuf, DoubleJumpPayload> CODEC = PacketCodec.unit(INSTANCE);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
