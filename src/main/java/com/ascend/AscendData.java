package com.ascend;

import com.ascend.logic.PlayerProgress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Saves each player's progress with the player, and keeps it on death. */
@SuppressWarnings("UnstableApiUsage")
public final class AscendData {
	public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("level").forGetter(PlayerProgress::level),
			Codec.INT.fieldOf("xp").forGetter(PlayerProgress::xp),
			Codec.INT.fieldOf("points").forGetter(PlayerProgress::points),
			Codec.INT.listOf().fieldOf("stats").forGetter(progress -> Arrays.stream(progress.stats()).boxed().toList())
	).apply(instance, (level, xp, points, stats) ->
			new PlayerProgress(level, xp, points, stats.stream().mapToInt(Integer::intValue).toArray())));

	public static final AttachmentType<PlayerProgress> PROGRESS = AttachmentRegistry.<PlayerProgress>builder()
			.persistent(CODEC)
			.copyOnDeath()
			.initializer(PlayerProgress::new)
			.buildAndRegister(Ascend.id("progress"));

	private AscendData() {
	}

	/** Loads the class so the attachment is registered during mod initialization. */
	public static void init() {
	}
}
