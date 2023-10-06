package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerActiveAbilityPacket
{
	private final ItemStack stack;

	public TriggerActiveAbilityPacket(ItemStack stack)
	{
		this.stack = stack;
	}

	public static void encode(TriggerActiveAbilityPacket packet, FriendlyByteBuf buf)
	{
		buf.writeItem(packet.stack);
	}

	public static TriggerActiveAbilityPacket decode(FriendlyByteBuf buf)
	{
		return new TriggerActiveAbilityPacket(buf.readItem());
	}

	public static class TriggerActiveAbilityPacketHandler
	{
		public static void handle(TriggerActiveAbilityPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.stack, ctx.get().getSender()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(ItemStack stack, ServerPlayer entityPlayer) implements Runnable
	{
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				CyberwareAPI.useActiveItem(entityPlayer, cyberwareUserData.getCyberware(stack));
			}
		}
	}
}
