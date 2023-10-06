package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScannerSmashPacket
{
	private final BlockPos pos;

	public ScannerSmashPacket(BlockPos pos)
	{
		this.pos = pos;
	}

	public static void encode(ScannerSmashPacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
	}

	public static ScannerSmashPacket decode(FriendlyByteBuf buf)
	{
		return new ScannerSmashPacket(buf.readBlockPos());
	}

	public static class ScannerSmashPacketHandler
	{
		public static void handle(ScannerSmashPacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.pos));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos) implements Runnable
	{
		@Override
		public void run()
		{
			Level world = Minecraft.getInstance().level;

			if (world != null)
			{
				BlockEntity te = world.getBlockEntity(pos);
				if (te instanceof TileEntityEngineeringTable eng)
				{
					eng.smashSounds();
				}
			}
		}
	}
}
