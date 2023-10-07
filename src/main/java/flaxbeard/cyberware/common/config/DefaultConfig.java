package flaxbeard.cyberware.common.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DefaultConfig
{
	public static float ENGINEERING_CHANCE = 15F;
	public static float SCANNER_CHANCE = 10F;
	public static float SCANNER_CHANCE_ADDL = 10F;
	public static int SCANNER_TIME = 24000;
	public static int ESSENCE = 100;
	public static int CRITICAL_ESSENCE = 25;
	public static boolean MOBS_ENABLE_CYBER_ZOMBIES = true;
	public static int MOBS_CYBER_ZOMBIE_WEIGHT = 15;
	public static int MOBS_CYBER_ZOMBIE_MIN_PACK = 1;
	public static int MOBS_CYBER_ZOMBIE_MAX_PACK = 1;
	public static List<ResourceKey<Level>> MOBS_DIMENSION_IDS = new ArrayList<>();
	public static boolean MOBS_IS_DIMENSION_BLACKLIST = true;
	public static boolean MOBS_APPLY_DIMENSION_TO_SPAWNING = true;
	public static boolean MOBS_APPLY_DIMENSION_TO_BEACON = true;
	public static boolean MOBS_ADD_CLOTHES = true;
	public static float MOBS_CYBER_ZOMBIE_DROP_RARITY = 50.0F;
	public static float MOBS_CLOTH_DROP_RARITY = 50.0F;
	public static int HUDR = 76;
	public static int HUDG = 255;
	public static int HUDB = 0;
	public static boolean ENABLE_FLOAT = false;
	public static float HUDLENS_FLOAT = 0.1F;
	public static float HUDJACK_FLOAT = 0.05F;
	public static boolean SURGERY_CRAFTING = false;
	public static boolean DEFAULT_DROP = false;
	public static boolean DEFAULT_KEEP = false;
	public static float DROP_CHANCE = 100F;
	public static boolean ENABLE_KATANA = true;
	public static boolean ENABLE_CLOTHES = true;
	public static boolean ENABLE_CUSTOM_PLAYER_MODEL = true;
	public static int TESLA_PER_POWER = 1;
	public static String FIST_MINING_TOOL_NAME = "minecraft:iron_pickaxe";
	public static boolean INT_ENDER_IO = true;
	public static boolean INT_TOUGH_AS_NAILS = true;
	public static boolean INT_BOTANIA = true;
	public static boolean INT_MATTER_OVERDRIVE = true;
}
