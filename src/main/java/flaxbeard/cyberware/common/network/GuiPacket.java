package flaxbeard.cyberware.common.network;

import net.minecraft.server.level.ServerPlayer;

// TODO: needs entirely rethinking
public class GuiPacket
{
	private final int guid;
	private final int x;
	private final int y;
	private final int z;

	public GuiPacket(int guid, int x, int y, int z)
	{
		this.guid = guid;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private record DoSync(int guid, int x, int y, int z, ServerPlayer playerEntity) implements Runnable
	{
		@Override
		public void run()
		{
			// TODO
			// serverPlayer.openGui(Cyberware.INSTANCE, guid, playerEntity.level, x, y, z);
		}
	}
}
