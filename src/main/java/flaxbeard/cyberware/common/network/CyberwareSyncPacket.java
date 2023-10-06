package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.hud.CyberwareHudDataEvent;
import flaxbeard.cyberware.api.hud.IHudElement;
import flaxbeard.cyberware.client.gui.hud.HudNBTData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class CyberwareSyncPacket
{
	private final CompoundTag data;
	private final int entityId;

	public CyberwareSyncPacket(int entityId, CompoundTag data)
	{
		this.data = data;
		this.entityId = entityId;
	}

	public CyberwareSyncPacket(CompoundTag data, int entityId)
	{
		this.data = data;
		this.entityId = entityId;
	}

	public static void encode(CyberwareSyncPacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.entityId);
		buf.writeNbt(packet.data);
	}

	public static CyberwareSyncPacket decode(FriendlyByteBuf buf)
	{
		return new CyberwareSyncPacket(buf.readInt(), buf.readNbt());
	}

	public static class CyberwareSyncPacketHandler
	{
		public static void handle(CyberwareSyncPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.entityId, msg.data));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int entityId, CompoundTag data) implements Runnable
	{
		@Override
		public void run()
		{
			assert Minecraft.getInstance().level != null;
			Entity targetEntity = Minecraft.getInstance().level.getEntity(entityId);

			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(targetEntity);
			if (cyberwareUserData != null)
			{
				cyberwareUserData.deserializeNBT(data);

				if (targetEntity == Minecraft.getInstance().player)
				{
					CompoundTag tagCompound = cyberwareUserData.getHudData();

					CyberwareHudDataEvent hudEvent = new CyberwareHudDataEvent();
					MinecraftForge.EVENT_BUS.post(hudEvent);
					List<IHudElement> elements = hudEvent.getElements();

					for (IHudElement element : elements)
					{
						if (tagCompound.contains(element.getUniqueName()))
						{
							element.load(new HudNBTData((CompoundTag) tagCompound.get(element.getUniqueName())));
						}
					}
				}
			}
		}
	}
}
