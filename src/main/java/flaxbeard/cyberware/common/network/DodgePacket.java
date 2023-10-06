package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.hud.INotification;
import flaxbeard.cyberware.api.hud.NotificationInstance;
import flaxbeard.cyberware.common.handler.HudHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DodgePacket
{
	private final int entityId;

	public DodgePacket(int entityId)
	{
		this.entityId = entityId;
	}

	public static void encode(DodgePacket packet, FriendlyByteBuf buf)
	{
		buf.writeInt(packet.entityId);
	}

	public static DodgePacket decode(FriendlyByteBuf buf)
	{
		return new DodgePacket(buf.readInt());
	}

	public static class DodgePacketHandler
	{
		public static void handle(DodgePacket msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(new DoSync(msg.entityId));
			ctx.get().setPacketHandled(true);
		}
	}

	private record DoSync(int entityId) implements Runnable
	{
		@Override
		public void run()
		{
			ClientLevel level = Minecraft.getInstance().level;
			assert level != null;
			Entity targetEntity = level.getEntity(entityId);

			if (targetEntity != null)
			{
				for (int index = 0; index < 25; index++)
				{
					RandomSource rand = targetEntity.level.getRandom();
					// TODO
					//					level.spawnParticle(EnumParticleTypes.SPELL, targetEntity.posX, targetEntity
					//					.posY + rand.nextFloat() * targetEntity.height, targetEntity.posZ,
					//							(rand.nextFloat() - .5F) * .2F,
					//							0,
					//							(rand.nextFloat() - .5F) * .2F,
					//							255, 255, 255);
				}

				targetEntity.playSound(SoundEvents.FIREWORK_ROCKET_SHOOT, 1F, 1F);

				if (targetEntity == Minecraft.getInstance().player)
				{
					HudHandler.addNotification(new NotificationInstance(
						targetEntity.tickCount,
						new DodgeNotification()
					));
				}
			}
		}
	}

	private static class DodgeNotification implements INotification
	{
		@Override
		public void render(int x, int y)
		{
			// TODO: render
			//			Minecraft.getInstance().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
			//
			//			GlStateManager.pushMatrix();
			//			float[] color = CyberwareAPI.getHUDColor();
			//			GlStateManager.color(color[0], color[1], color[2]);
			//			ClientUtils.drawTexturedModalRect(x + 1, y + 1, 0, 39, 15, 14);
			//			GlStateManager.popMatrix();
		}

		@Override
		public int getDuration()
		{
			return 5;
		}
	}
}
