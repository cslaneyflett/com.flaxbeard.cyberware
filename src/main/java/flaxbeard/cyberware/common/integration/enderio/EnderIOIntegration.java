package flaxbeard.cyberware.common.integration.enderio;

import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Optional;

public class EnderIOIntegration
{
	public static final String MOD_ID = "enderio";

	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(new EnderIOIntegration());
	}

	@Optional.Method(modid = MOD_ID)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTeleportEntity(crazypants.enderio.api.teleport.TeleportEntityEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity entityLivingBase = (LivingEntity) event.getEntity();
		if (!ItemBrainUpgrade.isTeleportationAllowed(entityLivingBase))
		{
			event.setCanceled(true);
		}
	}
}
