package flaxbeard.cyberware.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParticlePacket
{
	private final int effectId;
	private final BlockPos pos;

	public ParticlePacket(int effectId, BlockPos pos)
	{
		this.effectId = effectId;
		this.pos = pos;
	}

	public static void encode(ParticlePacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.effectId);
		buf.writeBlockPos(packet.pos);
	}

	public static ParticlePacket decode(FriendlyByteBuf buf)
	{
		return new ParticlePacket(buf.readInt(), buf.readBlockPos());
	}

	public static class ParticlePacketHandler
	{
		public static void handle(ParticlePacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.effectId, msg.pos));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int effectId, BlockPos pos) implements Runnable
	{
		@Override
		public void run()
		{
			ClientLevel world = Minecraft.getInstance().level;

			// effectId: EnumParticleTypes.HEART, EnumParticleTypes.ANGRY_VILLAGER

			// TODO: particle
			if (world != null)
			{
				for (int index = 0; index < 5; index++)
				{
					// world.spawnParticle(
					// 		,
					// 		pos.x + world.rand.nextFloat() - 0.5F,
					// 		pos.y + world.rand.nextFloat() - 0.5F,
					// 		pos.z + world.rand.nextFloat() - 0.5F,
					// 		2.0F * (world.rand.nextFloat() - 0.5F),
					// 		0.5F,
					// 		2.0F * (world.rand.nextFloat() - 0.5F)
					// );
				}
			}
		}
	}
}
