package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.HotkeyHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHotkeyPacket
{
	private final int selectedPart;
	private final int key;

	public SyncHotkeyPacket(int selectedPart, int key)
	{
		this.selectedPart = selectedPart;
		this.key = key;
	}

	public static void encode(SyncHotkeyPacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.selectedPart);
		buf.writeInt(packet.key);
	}

	public static SyncHotkeyPacket decode(FriendlyByteBuf buf)
	{
		return new SyncHotkeyPacket(buf.readInt(), buf.readInt());
	}

	public static class SyncHotkeyPacketHandler
	{
		public static void handle(SyncHotkeyPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.selectedPart, msg.key, ctx.get().getSender()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int selectedPart, int key, ServerPlayer entityPlayer) implements Runnable
	{
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);

			if (cyberwareUserData != null)
			{
				if (key == Integer.MAX_VALUE)
				{
					HotkeyHelper.removeHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get(selectedPart));
				} else
				{
					HotkeyHelper.removeHotkey(cyberwareUserData, key);
					HotkeyHelper.assignHotkey(
						cyberwareUserData,
						cyberwareUserData.getActiveItems().get(selectedPart),
						key
					);
				}
			}
		}
	}
}
