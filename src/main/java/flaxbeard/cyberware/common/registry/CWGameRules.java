package flaxbeard.cyberware.common.registry;

import flaxbeard.cyberware.common.config.CyberwareConfig;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

// TODO: cursed
public class CWGameRules
{
	private CWGameRules()
	{
	}

	public static final GameRules.Key<GameRules.BooleanValue> KEEP_WARE_GAMERULE =
		GameRules.register("keepCyberware", GameRules.Category.UPDATES, create(CyberwareConfig.INSTANCE.DEFAULT_KEEP.get()));
	public static final GameRules.Key<GameRules.BooleanValue> DROP_WARE_GAMERULE =
		GameRules.register("keepCyberware", GameRules.Category.UPDATES, create(CyberwareConfig.INSTANCE.DEFAULT_DROP.get()));

	@SuppressWarnings("unchecked")
	public static GameRules.Type<GameRules.BooleanValue> create(boolean defaultValue)
	{
		try
		{
			Method createGameruleMethod = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "m_46250_", boolean.class);
			createGameruleMethod.setAccessible(true);
			return (GameRules.Type<GameRules.BooleanValue>) createGameruleMethod.invoke(GameRules.BooleanValue.class, defaultValue);
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
