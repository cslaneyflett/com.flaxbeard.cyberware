package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.lib.LibConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SurgeryRemovePacket
{
	private final BlockPos pos;
	private final ResourceKey<Level> dimensionKey;
	private final int slotNumber;
	private final boolean isNull;

	public SurgeryRemovePacket(BlockPos pos, ResourceKey<Level> dimensionKey, int slotNumber, boolean isNull)
	{
		this.pos = pos;
		this.dimensionKey = dimensionKey;
		this.slotNumber = slotNumber;
		this.isNull = isNull;
	}

	public static void encode(SurgeryRemovePacket packet, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(packet.pos);
		buf.writeResourceKey(packet.dimensionKey);
		buf.writeInt(packet.slotNumber);
		buf.writeBoolean(packet.isNull);
	}

	public static SurgeryRemovePacket decode(FriendlyByteBuf buf)
	{
		return new SurgeryRemovePacket(buf.readBlockPos(), buf.readResourceKey(Registry.DIMENSION_REGISTRY), buf.readInt(), buf.readBoolean());
	}

	public static class SurgeryRemovePacketHandler
	{
		public static void handle(SurgeryRemovePacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			// TODO: DimensionManager.getLevel(message.dimensionKey) for queue?
			ctx.get().enqueueWork(new DoSync(msg.pos, msg.dimensionKey, msg.slotNumber, msg.isNull));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(BlockPos pos, ResourceKey<Level> dimensionKey, int slotNumber,
						  boolean isNull) implements Runnable
	{
		@Override
		public void run()
		{
			// TODO: dimensions
			Level world = DimensionManager.getLevel(dimensionKey);

			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof TileEntitySurgery surgery)
			{
				surgery.discardSlots[slotNumber] = isNull;

				if (isNull)
				{
					surgery.disableDependants(
						surgery.slotsPlayer.getStackInSlot(slotNumber),
						EnumSlot.values()[slotNumber / 10],
						slotNumber % LibConstants.WARE_PER_SLOT
					);
				} else
				{
					surgery.enableDependsOn(
						surgery.slotsPlayer.getStackInSlot(slotNumber),
						EnumSlot.values()[slotNumber / 10], slotNumber % LibConstants.WARE_PER_SLOT
					);
				}
				surgery.updateEssential(EnumSlot.values()[slotNumber / LibConstants.WARE_PER_SLOT]);
				surgery.updateEssence();
			}
		}
	}
}
