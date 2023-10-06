package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenRadialMenuPacket
{
	public OpenRadialMenuPacket() {}

	public static void encode(OpenRadialMenuPacket packet, FriendlyByteBuf buf)
	{
		// nothing
	}

	public static OpenRadialMenuPacket decode(FriendlyByteBuf buf)
	{
		return new OpenRadialMenuPacket();
	}

	public static class OpenRadialMenuPacketHandler
	{
		public static void handle(OpenRadialMenuPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			// TODO: DimensionManager.getLevel(message.dimensionKey) for queue?
			ctx.get().enqueueWork(new DoSync(ctx.get().getSender()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(Player entityPlayer) implements Runnable
	{
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				cyberwareUserData.setOpenedRadialMenu(true);
			}
		}
	}
}
