package flaxbeard.cyberware.common.integration;

import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import net.minecraft.world.entity.player.Player;

public class CyberwareMatterOverdriveCheck
{
	public static boolean isPlayerAndroid(Player entityPlayer)
	{
		AndroidPlayer androidPlayer = MOPlayerCapabilityProvider.GetAndroidCapability(entityPlayer);
		if (androidPlayer != null && androidPlayer.isAndroid())
		{
			return true;
		}
		return false;
	}
}
