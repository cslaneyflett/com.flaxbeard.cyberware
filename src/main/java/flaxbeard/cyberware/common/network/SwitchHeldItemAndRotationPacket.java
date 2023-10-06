package flaxbeard.cyberware.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchHeldItemAndRotationPacket
{
	private final int slot;
	private final int entityId;
	private final int attackerId;

	public SwitchHeldItemAndRotationPacket(int slot, int entityId, int attackerId)
	{
		this.slot = slot;
		this.entityId = entityId;
		this.attackerId = attackerId;
	}

	public static void encode(SwitchHeldItemAndRotationPacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.slot);
		buf.writeInt(packet.entityId);
		buf.writeInt(packet.attackerId);
	}

	public static SwitchHeldItemAndRotationPacket decode(FriendlyByteBuf buf)
	{
		return new SwitchHeldItemAndRotationPacket(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static class SwitchHeldItemAndRotationPacketHandler
	{
		public static void handle(SwitchHeldItemAndRotationPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new SwitchHeldItemAndRotationPacket.DoSync(msg.slot, msg.slot, msg.attackerId));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int entityId, int slot, int attackerId) implements Runnable
	{
		@Override
		public void run()
		{
			assert Minecraft.getInstance().level != null;
			Entity targetEntity = Minecraft.getInstance().level.getEntity(entityId);

			if (targetEntity instanceof Player player)
			{
				// TODO: this right?
				player.getInventory().pickSlot(slot);

				if (attackerId != -1)
				{
					player.closeContainer(); // TODO: this right?
					Entity facingEntity = Minecraft.getInstance().level.getEntity(attackerId);

					if (facingEntity != null)
					{
						targetEntity.lookAt(EntityAnchorArgument.Anchor.EYES, facingEntity.getEyePosition());
					}
				}
			}
		}
	}
}
