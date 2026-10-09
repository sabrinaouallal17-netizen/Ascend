package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Server to client: learned skills (ordinals), slots (ordinal or -1) and cooldown ticks left for each skill.
 */
public record SkillsPayload(List<Integer> learned, List<Integer> slots, List<Integer> cooldowns) implements CustomPayload {
	public static final CustomPayload.Id<SkillsPayload> ID = new CustomPayload.Id<>(Ascend.id("skills"));
	public static final PacketCodec<ByteBuf, SkillsPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), SkillsPayload::learned,
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), SkillsPayload::slots,
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), SkillsPayload::cooldowns,
			SkillsPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
