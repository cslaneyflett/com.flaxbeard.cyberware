package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.client.gui.*;
import flaxbeard.cyberware.common.block.tile.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int id, Player entityPlayer, Level world, int x, int y, int z)
	{
		switch (id)
		{
			case 0:
				return new ContainerSurgery(
					entityPlayer.inventory,
					(TileEntitySurgery) world.getBlockEntity(new BlockPos(x, y, z))
				);
			case 1:
				return new ContainerFineManipulators(entityPlayer.inventory, true, entityPlayer);
			case 2:
				return new ContainerEngineeringTable(entityPlayer.getCachedUniqueIdString(), entityPlayer.inventory,
					(TileEntityEngineeringTable) world.getBlockEntity(new BlockPos(
						x,
						y,
						z
					))
				);
			case 3:
				return new ContainerScanner(
					entityPlayer.inventory,
					(TileEntityScanner) world.getBlockEntity(new BlockPos(x, y, z))
				);
			case 4:
				return new ContainerBlueprintArchive(
					entityPlayer.inventory,
					(TileEntityBlueprintArchive) world.getBlockEntity(new BlockPos(
						x,
						y,
						z
					))
				);
			case 5:
				return new ContainerComponentBox(
					entityPlayer.inventory,
					(TileEntityComponentBox) world.getBlockEntity(new BlockPos(x, y, z))
				);
			default:
				return new ContainerComponentBox(
					entityPlayer.inventory,
					entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem)
				);
		}
	}

	@Override
	public Object getClientGuiElement(int id, Player entityPlayer, Level world, int x, int y, int z)
	{
		switch (id)
		{
			case 0:
				return new GuiSurgery(entityPlayer.inventory, (TileEntitySurgery) world.getBlockEntity(new BlockPos(
					x,
					y,
					z
				)));
			case 1:
				return new GuiFineManipulators(entityPlayer, new ContainerFineManipulators(entityPlayer.inventory,
					false, entityPlayer
				));
			case 2:
				return new GuiEngineeringTable(
					entityPlayer.inventory,
					(TileEntityEngineeringTable) world.getBlockEntity(new BlockPos(x, y,
						z
					))
				);
			case 3:
				return new GuiScanner(entityPlayer.inventory, (TileEntityScanner) world.getBlockEntity(new BlockPos(
					x,
					y,
					z
				)));
			case 4:
				return new GuiBlueprintArchive(
					entityPlayer.inventory,
					(TileEntityBlueprintArchive) world.getBlockEntity(new BlockPos(x, y,
						z
					))
				);
			case 5:
				return new GuiComponentBox(
					entityPlayer.inventory,
					(TileEntityComponentBox) world.getBlockEntity(new BlockPos(x, y, z))
				);
			default:
				return new GuiComponentBox(
					entityPlayer.inventory,
					entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem)
				);
		}
	}
}
