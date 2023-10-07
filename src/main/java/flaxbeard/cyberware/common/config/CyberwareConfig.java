package flaxbeard.cyberware.common.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

// TODO: networking
public class CyberwareConfig
{
	public static final CyberwareConfig INSTANCE;
	public static final ForgeConfigSpec INSTANCE_SPEC;

	static
	{
		Pair<CyberwareConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
			.configure(CyberwareConfig::new);

		INSTANCE = pair.getLeft();
		INSTANCE_SPEC = pair.getRight();
	}

	public final ForgeConfigSpec.ConfigValue<List<? extends ResourceKey<Level>>> MOBS_DIMENSION_IDS;
	public final ForgeConfigSpec.BooleanValue MOBS_ENABLE_CYBER_ZOMBIES;
	public final ForgeConfigSpec.IntValue MOBS_CYBER_ZOMBIE_WEIGHT;
	public final ForgeConfigSpec.IntValue MOBS_CYBER_ZOMBIE_MIN_PACK;
	public final ForgeConfigSpec.IntValue MOBS_CYBER_ZOMBIE_MAX_PACK;
	public final ForgeConfigSpec.BooleanValue MOBS_IS_DIMENSION_BLACKLIST;
	public final ForgeConfigSpec.BooleanValue MOBS_APPLY_DIMENSION_TO_SPAWNING;
	public final ForgeConfigSpec.BooleanValue MOBS_APPLY_DIMENSION_TO_BEACON;
	public final ForgeConfigSpec.BooleanValue MOBS_ADD_CLOTHES;
	public final ForgeConfigSpec.DoubleValue MOBS_CYBER_ZOMBIE_DROP_RARITY;
	public final ForgeConfigSpec.DoubleValue MOBS_CLOTH_DROP_RARITY;
	public final ForgeConfigSpec.BooleanValue SURGERY_CRAFTING;
	public final ForgeConfigSpec.IntValue TESLA_PER_POWER;
	public final ForgeConfigSpec.IntValue ESSENCE;
	public final ForgeConfigSpec.IntValue CRITICAL_ESSENCE;
	public final ForgeConfigSpec.BooleanValue DEFAULT_DROP;
	public final ForgeConfigSpec.BooleanValue DEFAULT_KEEP;
	public final ForgeConfigSpec.DoubleValue DROP_CHANCE;
	public final ForgeConfigSpec.DoubleValue ENGINEERING_CHANCE;
	public final ForgeConfigSpec.DoubleValue SCANNER_CHANCE;
	public final ForgeConfigSpec.DoubleValue SCANNER_CHANCE_ADDL;
	public final ForgeConfigSpec.IntValue SCANNER_TIME;
	public final ForgeConfigSpec.BooleanValue ENABLE_KATANA;
	public final ForgeConfigSpec.BooleanValue ENABLE_CLOTHES;
	public final ForgeConfigSpec.BooleanValue ENABLE_CUSTOM_PLAYER_MODEL;
	public final ForgeConfigSpec.BooleanValue ENABLE_FLOAT;
	public final ForgeConfigSpec.DoubleValue HUDJACK_FLOAT;
	public final ForgeConfigSpec.DoubleValue HUDLENS_FLOAT;
	public final ForgeConfigSpec.BooleanValue INT_ENDER_IO;
	public final ForgeConfigSpec.BooleanValue INT_TOUGH_AS_NAILS;
	public final ForgeConfigSpec.BooleanValue INT_BOTANIA;
	public final ForgeConfigSpec.BooleanValue INT_MATTER_OVERDRIVE;
	//    public final ForgeConfigSpec.Value FIST_MINING_TOOL_NAME;

	CyberwareConfig(ForgeConfigSpec.Builder builder)
	{
		MOBS_DIMENSION_IDS = builder
			.comment("Dimensions ids")
			.defineList("MOBS_DIMENSION_IDS", DefaultConfig.MOBS_DIMENSION_IDS, o -> true)
		;

		MOBS_ENABLE_CYBER_ZOMBIES = builder
			.comment("CyberZombies are enabled")
			.define("MOBS_ENABLE_CYBER_ZOMBIES", DefaultConfig.MOBS_ENABLE_CYBER_ZOMBIES)
		;

		// INT "Vanilla Zombie = 100, Enderman = 10, Witch = 5"
		MOBS_CYBER_ZOMBIE_WEIGHT = builder
			.comment("CyberZombies spawning weight")
			.defineInRange("MOBS_ENABLE_CYBER_ZOMBIES", DefaultConfig.MOBS_CYBER_ZOMBIE_WEIGHT, 0,
				Integer.MAX_VALUE
			)
		;

		// INT "Vanilla Zombie = 4, Enderman = 1, Witch = 1"
		MOBS_CYBER_ZOMBIE_MIN_PACK = builder
			.comment("CyberZombies minimum pack size")
			.defineInRange("MOBS_CYBER_ZOMBIE_MIN_PACK", DefaultConfig.MOBS_CYBER_ZOMBIE_MIN_PACK, 0,
				Integer.MAX_VALUE
			)
		;

		// INT "Vanilla Zombie = 4, Enderman = 4, Witch = 1"
		MOBS_CYBER_ZOMBIE_MAX_PACK = builder
			.comment("CyberZombies maximum pack size")
			.defineInRange("MOBS_CYBER_ZOMBIE_MAX_PACK", DefaultConfig.MOBS_CYBER_ZOMBIE_MAX_PACK, 0,
				Integer.MAX_VALUE
			)
		;

		// BOOL ""
		MOBS_IS_DIMENSION_BLACKLIST = builder
			.comment("Dimension ids is a blacklist?")
			.define("MOBS_IS_DIMENSION_BLACKLIST", DefaultConfig.MOBS_IS_DIMENSION_BLACKLIST)
		;
		// BOOL ""
		MOBS_APPLY_DIMENSION_TO_SPAWNING = builder
			.comment("Dimension ids applies to natural spawning?")
			.define("MOBS_APPLY_DIMENSION_TO_SPAWNING", DefaultConfig.MOBS_APPLY_DIMENSION_TO_SPAWNING)
		;
		// BOOL ""
		MOBS_APPLY_DIMENSION_TO_BEACON = builder
			.comment("Dimension ids applies to beacon, radio & cranial broadcaster?")
			.define("MOBS_APPLY_DIMENSION_TO_BEACON", DefaultConfig.MOBS_APPLY_DIMENSION_TO_BEACON)
		;

		// BOOL ""
		MOBS_ADD_CLOTHES = builder
			.comment("Add Cyberware clothing to mobs")
			.define("MOBS_ADD_CLOTHES", DefaultConfig.MOBS_ADD_CLOTHES)
		;
		// FLOAT ""
		MOBS_CYBER_ZOMBIE_DROP_RARITY = builder
			.comment("Percent chance a CyberZombie drops a cyberware")
			.defineInRange("MOBS_CYBER_ZOMBIE_DROP_RARITY", DefaultConfig.MOBS_CYBER_ZOMBIE_DROP_RARITY, 0F, 100F)
		;
		// FLOAT ""
		MOBS_CLOTH_DROP_RARITY = builder
			.comment("Percent chance a Cyberware clothing is dropped")
			.defineInRange("MOBS_CLOTH_DROP_RARITY", DefaultConfig.MOBS_CLOTH_DROP_RARITY, 0F, 100F)
		;

		// BOOL "Normally only found in Nether fortresses"
		SURGERY_CRAFTING = builder
			.comment("Enable crafting recipe for Robosurgeon")
			.define("SURGERY_CRAFTING", DefaultConfig.SURGERY_CRAFTING)
		;
		// INT ""
		TESLA_PER_POWER = builder
			.comment("RF/Tesla per internal power unit")
			.defineInRange("TESLA_PER_POWER", DefaultConfig.TESLA_PER_POWER, 0, Integer.MAX_VALUE)
		;

		// INT ""
		ESSENCE = builder
			.comment("Maximum Essence")
			.defineInRange("ESSENCE", DefaultConfig.ESSENCE, 0, Integer.MAX_VALUE)
		;
		// INT ""
		CRITICAL_ESSENCE = builder
			.comment("Critical Essence value, where rejection begins")
			.defineInRange("CRITICAL_ESSENCE", DefaultConfig.CRITICAL_ESSENCE, 0, Integer.MAX_VALUE)
		;

		// BOOL "Determines if players drop their Cyberware on death. Does not change settings on existing worlds, use
		// /gamerule for that. Overridden if cyberware_keepCyberware is true"
		DEFAULT_DROP = builder
			.comment("Default for gamerule cyberware_dropCyberware")
			.define("DEFAULT_DROP", DefaultConfig.DEFAULT_DROP)
		;
		// BOOL "Determines if players keep their Cyberware between lives. Does not change settings on existing
		// worlds, use /gamerule for that."
		DEFAULT_KEEP = builder
			.comment("Default for gamerule cyberware_keepCyberware")
			.define("DEFAULT_KEEP", DefaultConfig.DEFAULT_KEEP)
		;

		// FLOAT "If dropCyberware enabled, chance for a piece of Cyberware to successfuly drop instead of being
		// destroyed."
		DROP_CHANCE = builder
			.comment("Chance of successful drop")
			.defineInRange("DROP_CHANCE", DefaultConfig.DROP_CHANCE, 0F, 100F)
		;

		// FLOAT ""
		ENGINEERING_CHANCE = builder
			.comment("Chance of blueprint from Engineering Table")
			.defineInRange("ENGINEERING_CHANCE", DefaultConfig.ENGINEERING_CHANCE, 0, 100F)
		;
		// FLOAT ""
		SCANNER_CHANCE = builder
			.comment("Chance of blueprint from Scanner")
			.defineInRange("SCANNER_CHANCE", DefaultConfig.SCANNER_CHANCE, 0, 50F)
		;
		// FLOAT ""
		SCANNER_CHANCE_ADDL = builder
			.comment("Additive chance for Scanner per extra item")
			.defineInRange("SCANNER_CHANCE_ADDL", DefaultConfig.SCANNER_CHANCE_ADDL, 0, 100F)
		;
		// INT "24000 is one Minecraft day, 1200 is one real-life minute"
		SCANNER_TIME = builder
			.comment("Ticks taken per Scanner operation")
			.defineInRange("SCANNER_TIME", DefaultConfig.SCANNER_TIME, 0, Integer.MAX_VALUE)
		;

		// BOOL ""
		ENABLE_KATANA = builder
			.comment("Enable Katana")
			.define("ENABLE_KATANA", DefaultConfig.ENABLE_KATANA)
		;
		// BOOL ""
		ENABLE_CLOTHES = builder
			.comment("Enable Trench Coat, Mirror Shades, and Biker Jacket")
			.define("ENABLE_CLOTHES", DefaultConfig.ENABLE_CLOTHES)
		;
		// BOOL ""
		ENABLE_CUSTOM_PLAYER_MODEL = builder
			.comment("Enable changes to player model (missing skin, missing limbs, Cybernetic limbs)")
			.define("ENABLE_CUSTOM_PLAYER_MODEL", DefaultConfig.ENABLE_CUSTOM_PLAYER_MODEL)
		;

		// BOOL "Experimental, defaults to false."
		ENABLE_FLOAT = builder
			.comment("Enable hudlens and hudjack float.")
			.define("ENABLE_FLOAT", DefaultConfig.ENABLE_FLOAT)
		;

		// FLOAT ""
		HUDJACK_FLOAT = builder
			.comment("Amount hudjack HUD will 'float' with movement. Set to 0 for no float.")
			.defineInRange("HUDJACK_FLOAT", DefaultConfig.HUDJACK_FLOAT, 0F, 100F)
		;
		// FLOAT ""
		HUDLENS_FLOAT = builder
			.comment("Amount hudlens HUD will 'float' with movement. Set to 0 for no float.")
			.defineInRange("HUDLENS_FLOAT", DefaultConfig.HUDLENS_FLOAT, 0F, 100F)
		;

		// BOOL "Requires EnderIO"
		INT_ENDER_IO = builder
			.comment("Enable EnderIO Integration if the mod is Loaded")
			.define("INT_ENDER_IO", DefaultConfig.INT_ENDER_IO)
		;
		// BOOL "Requires Tough as Nails"
		INT_TOUGH_AS_NAILS = builder
			.comment("Enable Tough As Nails Integration if the mod is Loaded")
			.define("INT_TOUGH_AS_NAILS", DefaultConfig.INT_TOUGH_AS_NAILS)
		;
		// BOOL "Requires Botania"
		INT_BOTANIA = builder
			.comment("Enable Botania Integration if the mod is Loaded")
			.define("INT_BOTANIA", DefaultConfig.INT_BOTANIA)
		;
		// BOOL "Requires Matter Overdrive"
		INT_MATTER_OVERDRIVE = builder
			.comment("Enable Matter Overdrive Integration if the mod is Loaded")
			.define("INT_MATTER_OVERDRIVE", DefaultConfig.INT_MATTER_OVERDRIVE)
		;

		//        FIST_MINING_TOOL_NAME = config.getString("Registry name of the mining tool equivalent to the
		//        reinforced fist", C_OTHER, FIST_MINING_TOOL_NAME, "");

		// BOOL "Requires Matter Overdrive"
		//        FIST_MINING_TOOL_NAME = builder
		//                .comment("Enable Matter Overdrive Integration if the mod is Loaded")
		//                .define("FIST_MINING_TOOL_NAME", DefaultConfig.FIST_MINING_TOOL_NAME)
		;
	}
}