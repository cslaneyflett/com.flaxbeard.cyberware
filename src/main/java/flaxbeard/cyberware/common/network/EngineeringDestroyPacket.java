package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EngineeringDestroyPacket
{
	private final BlockPos pos;
	private final ResourceKey<Level> dimensionKey;

	public EngineeringDestroyPacket(BlockPos pos, ResourceKey<Level> dimensionKey)
	{
		this.pos = pos;
		this.dimensionKey = dimensionKey;
	}

	public static void encode(EngineeringDestroyPacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
		buf.writeResourceKey(packet.dimensionKey);
	}

	public static EngineeringDestroyPacket decode(FriendlyByteBuf buf)
	{
		return new EngineeringDestroyPacket(buf.readBlockPos(), buf.readResourceKey(Registry.DIMENSION_REGISTRY));
	}

	public static class EngineeringDestroyPacketHandler
	{
		public static void handle(EngineeringDestroyPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			// TODO: DimensionManager.getLevel(message.dimensionKey) for queue?
			ctx.get().enqueueWork(new EngineeringDestroyPacket.DoSync(msg.pos, msg.dimensionKey));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos, ResourceKey<Level> dimensionKey) implements Runnable
	{
		@Override
		public void run()
		{
			// TODO: dimension
			Level world = DimensionManager.getLevel(dimensionKey);
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof TileEntityEngineeringTable engineering)
			{
				engineering.smash(true);
			}
		}
	}
}
