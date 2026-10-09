package com.ascend;

import com.ascend.logic.Achievement;
import com.ascend.logic.Counter;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.QuestLog;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
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

	public static final Codec<SkillBook> SKILLS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("learned").forGetter(book -> book.learned().stream().map(Enum::name).toList()),
			Codec.STRING.listOf().fieldOf("slots").forGetter(book ->
					Arrays.stream(book.slots()).map(skill -> skill == null ? "" : skill.name()).toList())
	).apply(instance, (learned, slots) -> {
		Set<Skill> skills = EnumSet.noneOf(Skill.class);
		learned.forEach(name -> parse(Skill.class, name).ifPresent(skills::add));
		Skill[] slotArray = new Skill[SkillBook.SLOTS];
		for (int i = 0; i < slotArray.length && i < slots.size(); i++) {
			slotArray[i] = parse(Skill.class, slots.get(i)).orElse(null);
		}
		return new SkillBook(skills, slotArray);
	}));

	public static final Codec<QuestLog> QUESTS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("counters").forGetter(log -> {
				Map<String, Integer> counters = new HashMap<>();
				log.counters().forEach((counter, value) -> counters.put(counter.name(), value));
				return counters;
			}),
			Codec.STRING.listOf().fieldOf("achievements").forGetter(log -> log.unlocked().stream().map(Enum::name).toList())
	).apply(instance, (counters, achievements) -> {
		Map<Counter, Integer> parsedCounters = new EnumMap<>(Counter.class);
		counters.forEach((name, value) -> parse(Counter.class, name).ifPresent(counter -> parsedCounters.put(counter, value)));
		Set<Achievement> parsedAchievements = EnumSet.noneOf(Achievement.class);
		achievements.forEach(name -> parse(Achievement.class, name).ifPresent(parsedAchievements::add));
		return new QuestLog(parsedCounters, parsedAchievements);
	}));

	public static final AttachmentType<SkillBook> SKILLS = AttachmentRegistry.<SkillBook>builder()
			.persistent(SKILLS_CODEC)
			.copyOnDeath()
			.initializer(SkillBook::new)
			.buildAndRegister(Ascend.id("skills"));

	public static final AttachmentType<QuestLog> QUESTS = AttachmentRegistry.<QuestLog>builder()
			.persistent(QUESTS_CODEC)
			.copyOnDeath()
			.initializer(QuestLog::new)
			.buildAndRegister(Ascend.id("quests"));

	/** A hostile mob's level (set once, when it first loads). */
	public static final AttachmentType<Integer> MOB_LEVEL = AttachmentRegistry.<Integer>builder()
			.persistent(Codec.INT)
			.buildAndRegister(Ascend.id("mob_level"));

	/** Biomes the player has already discovered (each gives XP once). */
	public static final AttachmentType<Set<String>> BIOMES = AttachmentRegistry.<Set<String>>builder()
			.persistent(Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), set -> new ArrayList<>(set)))
			.copyOnDeath()
			.initializer(HashSet::new)
			.buildAndRegister(Ascend.id("biomes"));

	private AscendData() {
	}

	private static <E extends Enum<E>> Optional<E> parse(Class<E> type, String name) {
		try {
			return name.isEmpty() ? Optional.empty() : Optional.of(Enum.valueOf(type, name));
		} catch (IllegalArgumentException unknown) {
			return Optional.empty();
		}
	}

	/** Loads the class so the attachment is registered during mod initialization. */
	public static void init() {
	}
}
