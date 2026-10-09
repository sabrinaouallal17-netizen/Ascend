package com.ascend.network;

import com.ascend.Ascend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Client to server: learn a skill, bind it to a slot, or use the skill in a slot. */
public record SkillActionPayload(int action, int skill, int slot) implements CustomPayload {
	public static final int LEARN = 0;
	public static final int BIND = 1;
	public static final int USE = 2;

	public static final CustomPayload.Id<SkillActionPayload> ID = new CustomPayload.Id<>(Ascend.id("skill_action"));
	public static final PacketCodec<ByteBuf, SkillActionPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, SkillActionPayload::action,
			PacketCodecs.VAR_INT, SkillActionPayload::skill,
			PacketCodecs.VAR_INT, SkillActionPayload::slot,
			SkillActionPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
