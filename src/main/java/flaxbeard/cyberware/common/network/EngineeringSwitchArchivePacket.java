package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.client.gui.ContainerEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EngineeringSwitchArchivePacket
{
	private final BlockPos pos;
	private final ResourceKey<Level> dimensionKey;
	private final int entityId;
	private final boolean direction;
	private final boolean isComponent;

	public EngineeringSwitchArchivePacket(BlockPos pos, Player entityPlayer, boolean direction, boolean isComponent)
	{
		this.dimensionKey = entityPlayer.level.dimension();
		this.entityId = entityPlayer.getId();
		this.pos = pos;
		this.direction = direction;
		this.isComponent = isComponent;
	}

	EngineeringSwitchArchivePacket(BlockPos pos, ResourceKey<Level> dimensionKey, boolean direction, boolean isComponent, int entityId)
	{
		this.dimensionKey = dimensionKey;
		this.entityId = entityId;
		this.pos = pos;
		this.direction = direction;
		this.isComponent = isComponent;
	}

	public static void encode(EngineeringSwitchArchivePacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
		buf.writeResourceKey(packet.dimensionKey);
		buf.writeBoolean(packet.direction);
		buf.writeBoolean(packet.isComponent);
		buf.writeInt(packet.entityId);
	}

	public static EngineeringSwitchArchivePacket decode(FriendlyByteBuf buf)
	{
		return new EngineeringSwitchArchivePacket(buf.readBlockPos(), buf.readResourceKey(Registry.DIMENSION_REGISTRY), buf.readBoolean(), buf.readBoolean(), buf.readInt());
	}

	public static class EngineeringSwitchArchivePacketHandler
	{
		public static void handle(EngineeringSwitchArchivePacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			// TODO: DimensionManager.getLevel(message.dimensionKey) for queue?
			ctx.get().enqueueWork(new DoSync(msg.pos, msg.dimensionKey, msg.entityId, msg.direction, msg.isComponent));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos, ResourceKey<Level> dimensionKey, int entityId, boolean direction,
						  boolean isComponent) implements Runnable
	{
		@Override
		public void run()
		{
			// TODO: dimension
			Level world = DimensionManager.getLevel(dimensionKey);
			Entity entity = world.getEntity(entityId);

			if (entity instanceof Player entityPlayer)
			{
				if (isComponent)
				{
					if (entityPlayer.containerMenu instanceof ContainerEngineeringTable containerEngineeringTable)
					{
						if (direction)
						{
							containerEngineeringTable.nextComponentBox();
						} else
						{
							containerEngineeringTable.prevComponentBox();
						}

						// TileEntityEngineeringTable te = (TileEntityEngineeringTable) world.getBlockEntity(pos);
						//te.lastPlayerArchive.put(entityPlayer.getCachedUniqueIdString(), (
						// (ContainerEngineeringTable) entityPlayer.containerMenu).archive.getPos());
					}
				} else
				{
					if (entityPlayer.containerMenu instanceof ContainerEngineeringTable containerEngineeringTable)
					{
						if (direction)
						{
							containerEngineeringTable.nextArchive();
						} else
						{
							containerEngineeringTable.prevArchive();
						}

						TileEntityEngineeringTable te = (TileEntityEngineeringTable) world.getBlockEntity(pos);
						assert te != null;

						te.lastPlayerArchive.put(entityPlayer.getStringUUID(), containerEngineeringTable.archive.getBlockPos());
					}
				}
			}
		}
	}
}
