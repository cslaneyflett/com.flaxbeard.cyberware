package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EngineeringDestroyPacket
{
	private final BlockPos pos;

	public EngineeringDestroyPacket(BlockPos pos)
	{
		this.pos = pos;
	}

	public static void encode(EngineeringDestroyPacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
	}

	public static EngineeringDestroyPacket decode(FriendlyByteBuf buf)
	{
		return new EngineeringDestroyPacket(buf.readBlockPos());
	}

	public static class EngineeringDestroyPacketHandler
	{
		public static void handle(EngineeringDestroyPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new EngineeringDestroyPacket.DoSync(msg.pos, ctx.get().getSender().getLevel()));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos, Level level) implements Runnable
	{
		@Override
		public void run()
		{
			BlockEntity te = level.getBlockEntity(pos);

			if (te instanceof TileEntityEngineeringTable engineering)
			{
				engineering.smash(true);
			}
		}
	}
}
