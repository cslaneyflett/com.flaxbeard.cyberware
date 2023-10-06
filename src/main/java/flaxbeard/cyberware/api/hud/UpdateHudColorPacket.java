package flaxbeard.cyberware.api.hud;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateHudColorPacket
{
	private final int color;

	public UpdateHudColorPacket(int color)
	{
		this.color = color;
	}

	public static void encode(UpdateHudColorPacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.color);
	}

	public static UpdateHudColorPacket decode(FriendlyByteBuf buf)
	{
		return new UpdateHudColorPacket(buf.readInt());
	}

	public static class UpdateHudColorPacketHandler
	{
		public static void handle(UpdateHudColorPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.color, ctx.get().getSender()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int color, ServerPlayer entityPlayer) implements Runnable
	{
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				cyberwareUserData.setHudColor(color);
			}
		}
	}
}
