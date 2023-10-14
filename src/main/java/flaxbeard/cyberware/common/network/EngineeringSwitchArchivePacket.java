package flaxbeard.cyberware.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EngineeringSwitchArchivePacket
{
	private final BlockPos pos;
	private final int entityId;
	private final boolean direction;
	private final boolean isComponent;

	public EngineeringSwitchArchivePacket(BlockPos pos, Player entityPlayer, boolean direction, boolean isComponent)
	{
		this.entityId = entityPlayer.getId();
		this.pos = pos;
		this.direction = direction;
		this.isComponent = isComponent;
	}

	EngineeringSwitchArchivePacket(BlockPos pos, boolean direction, boolean isComponent, int entityId)
	{
		this.entityId = entityId;
		this.pos = pos;
		this.direction = direction;
		this.isComponent = isComponent;
	}

	public static void encode(EngineeringSwitchArchivePacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
		buf.writeBoolean(packet.direction);
		buf.writeBoolean(packet.isComponent);
		buf.writeInt(packet.entityId);
	}

	public static EngineeringSwitchArchivePacket decode(FriendlyByteBuf buf)
	{
		return new EngineeringSwitchArchivePacket(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean(), buf.readInt());
	}

	public static class EngineeringSwitchArchivePacketHandler
	{
		public static void handle(EngineeringSwitchArchivePacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			// TODO: DimensionManager.getLevel(message.dimensionKey) for queue?
			ctx.get().enqueueWork(new DoSync(msg.pos, ctx.get().getSender().getLevel(), msg.entityId, msg.direction, msg.isComponent));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos, Level world, int entityId, boolean direction,
						  boolean isComponent) implements Runnable
	{
		@Override
		public void run()
		{
			// TODO: dimension
			//			Level world = DimensionManager.getLevel(dimensionKey);
			Entity entity = world.getEntity(entityId);

			if (entity instanceof Player entityPlayer)
			{
				if (isComponent)
				{
					// TODO
					//					if (entityPlayer.containerMenu instanceof ContainerEngineeringTable containerEngineeringTable)
					//					{
					//						if (direction)
					//						{
					//							containerEngineeringTable.nextComponentBox();
					//						} else
					//						{
					//							containerEngineeringTable.prevComponentBox();
					//						}
					//
					//						// TileEntityEngineeringTable te = (TileEntityEngineeringTable) world.getBlockEntity(pos);
					//						//te.lastPlayerArchive.put(entityPlayer.getCachedUniqueIdString(), (
					//						// (ContainerEngineeringTable) entityPlayer.containerMenu).archive.getPos());
					//					}
				} else
				{
					// TODO
					//					if (entityPlayer.containerMenu instanceof ContainerEngineeringTable containerEngineeringTable)
					//					{
					//						if (direction)
					//						{
					//							containerEngineeringTable.nextArchive();
					//						} else
					//						{
					//							containerEngineeringTable.prevArchive();
					//						}
					//
					//						TileEntityEngineeringTable te = (TileEntityEngineeringTable) world.getBlockEntity(pos);
					//						assert te != null;
					//
					//						te.lastPlayerArchive.put(entityPlayer.getStringUUID(), containerEngineeringTable.archive.getBlockPos());
					//					}
				}
			}
		}
	}
}
