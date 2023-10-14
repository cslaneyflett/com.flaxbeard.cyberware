package flaxbeard.cyberware.common.handler;

public class GuiHandler // implements IGuiHandler
{
	//	@Override
	//	public Object getServerGuiElement(int id, Player entityPlayer, Level world, int x, int y, int z)
	//	{
	//		switch (id)
	//		{
	//			case 0:
	//				return new ContainerSurgery(
	//					entityPlayer.getInventory(),
	//					(TileEntitySurgery) world.getBlockEntity(new BlockPos(x, y, z))
	//				);
	//			case 1:
	//				return new ContainerFineManipulators(entityPlayer.getInventory(), true, entityPlayer);
	//			case 2:
	//				return new ContainerEngineeringTable(entityPlayer.getStringUUID(), entityPlayer.inventory,
	//					(TileEntityEngineeringTable) world.getBlockEntity(new BlockPos(
	//						x,
	//						y,
	//						z
	//					))
	//				);
	//			case 3:
	//				return new ContainerScanner(
	//					entityPlayer.inventory,
	//					(TileEntityScanner) world.getBlockEntity(new BlockPos(x, y, z))
	//				);
	//			case 4:
	//				return new ContainerBlueprintArchive(
	//					entityPlayer.inventory,
	//					(TileEntityBlueprintArchive) world.getBlockEntity(new BlockPos(
	//						x,
	//						y,
	//						z
	//					))
	//				);
	//			case 5:
	//				return new ContainerComponentBox(
	//					entityPlayer.inventory,
	//					(TileEntityComponentBox) world.getBlockEntity(new BlockPos(x, y, z))
	//				);
	//			default:
	//				return new ContainerComponentBox(
	//					entityPlayer.inventory,
	//					entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem)
	//				);
	//		}
	//	}
	//
	//	@Override
	//	public Object getClientGuiElement(int id, Player entityPlayer, Level world, int x, int y, int z)
	//	{
	//		switch (id)
	//		{
	//			case 0:
	//				return new GuiSurgery(entityPlayer.inventory, (TileEntitySurgery) world.getBlockEntity(new BlockPos(
	//					x,
	//					y,
	//					z
	//				)));
	//			case 1:
	//				return new GuiFineManipulators(entityPlayer, new ContainerFineManipulators(entityPlayer.inventory,
	//					false, entityPlayer
	//				));
	//			case 2:
	//				return new GuiEngineeringTable(
	//					entityPlayer.inventory,
	//					(TileEntityEngineeringTable) world.getBlockEntity(new BlockPos(x, y,
	//						z
	//					))
	//				);
	//			case 3:
	//				return new GuiScanner(entityPlayer.inventory, (TileEntityScanner) world.getBlockEntity(new BlockPos(
	//					x,
	//					y,
	//					z
	//				)));
	//			case 4:
	//				return new GuiBlueprintArchive(
	//					entityPlayer.inventory,
	//					(TileEntityBlueprintArchive) world.getBlockEntity(new BlockPos(x, y,
	//						z
	//					))
	//				);
	//			case 5:
	//				return new GuiComponentBox(
	//					entityPlayer.inventory,
	//					(TileEntityComponentBox) world.getBlockEntity(new BlockPos(x, y, z))
	//				);
	//			default:
	//				return new GuiComponentBox(
	//					entityPlayer.inventory,
	//					entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem)
	//				);
	//		}
	//	}
}
