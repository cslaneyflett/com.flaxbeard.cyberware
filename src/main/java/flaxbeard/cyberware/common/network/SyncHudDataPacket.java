package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHudDataPacket
{
	private final CompoundTag tagCompound;

	public SyncHudDataPacket(CompoundTag tagCompound)
	{
		this.tagCompound = tagCompound;
	}

	public static void encode(SyncHudDataPacket packet, FriendlyByteBuf buf)
	{
		buf.writeNbt(packet.tagCompound);
	}

	public static SyncHudDataPacket decode(FriendlyByteBuf buf)
	{
		return new SyncHudDataPacket(buf.readNbt());
	}

	public static class SyncHudDataPacketHandler
	{
		public static void handle(SyncHudDataPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.tagCompound, ctx.get().getSender()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(CompoundTag tagCompound, ServerPlayer entityPlayer) implements Runnable
	{
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);

			if (cyberwareUserData != null)
			{
				cyberwareUserData.setHudData(tagCompound);
			}
		}
	}
}
