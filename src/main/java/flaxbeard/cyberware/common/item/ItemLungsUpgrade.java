package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.item.base.CyberwareProperties;
import flaxbeard.cyberware.common.item.base.ItemCyberware;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.items.Lungs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemLungsUpgrade extends ItemCyberware
{
	public ItemLungsUpgrade(Properties itemProperties, CyberwareProperties cyberwareProperties)
	{
		super(itemProperties, cyberwareProperties, BodyRegionEnum.LUNGS);
	}

	@Override
	public int maximumStackSize(@Nonnull ItemStack stack)
	{
		return stack.is(Lungs.HYPER_OXYGENATION.get()) ? 3 : 1;
	}

	private int getAir(ItemStack stack)
	{
		CompoundTag tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (!tagCompound.contains("air"))
		{
			tagCompound.putInt("air", 900);
		}
		return tagCompound.getInt("air");
	}

	@Override
	public int getPowerConsumption(@Nonnull ItemStack stack)
	{
		return stack.is(Lungs.HYPER_OXYGENATION.get()) ?
			LibConstants.HYPEROXYGENATION_CONSUMPTION * stack.getCount() : 0;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (stack.is(Lungs.HYPER_OXYGENATION.get()))
		{
			switch (stack.getCount())
			{
				case 1:
					return 2;
				case 2:
					return 4;
				case 3:
					return 5;
			}
		}
		return super.getUnmodifiedEssenceCost(stack);
	}

	public static class EventHandler
	{
		// TODO: render
		//	import net.minecraft.client.gui.ScaledResolution;
		//	import net.minecraft.client.renderer.GlStateManager;
		//	import net.minecraftforge.client.event.RenderGameOverlayEvent;
		//	import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
		//	@OnlyIn(Dist.CLIENT)
		//	@SubscribeEvent
		//	public void onDrawScreenPost(RenderGameOverlayEvent.Post event)
		//	{
		//		if (event.getType() == ElementType.AIR)
		//		{
		//			Player entityPlayer = Minecraft.getInstance().player;
		//			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		//			if (cyberwareUserData == null) return;
		//
		//			ItemStack itemStackCompressedOxygen =
		//				cyberwareUserData.getCyberware(getCachedStack(META_COMPRESSED_OXYGEN));
		//			if (!itemStackCompressedOxygen.isEmpty()
		//				&& !entityPlayer.isCreative())
		//			{
		//				GlStateManager.pushMatrix();
		//				int air = getAir(itemStackCompressedOxygen);
		//
		//				Minecraft.getInstance().getTextureManager().bindTexture(Gui.ICONS);
		//
		//				ScaledResolution res = event.getResolution();
		//				GlStateManager.enableBlend();
		//				int left = res.getScaledWidth() / 2 + 91;
		//				int top = res.getScaledHeight() - 49 - 8;
		//
		//				float r = 1.0F;
		//				float b = 1.0F;
		//				float g = 1.0F;
		//
		//				if (entityPlayer.isInsideOfMaterial(Material.WATER))
		//				{
		//					while (air > 0)
		//					{
		//						r += 1.0F;
		//						b -= 0.25F;
		//						g += 0.25F;
		//						GlStateManager.color(r, g, b);
		//						int drawAir = Math.min(300, air);
		//						int full = Mth.ceil((drawAir - 2) * 10.0D / 300.0D);
		//						int partial = Mth.ceil(drawAir * 10.0D / 300.0D) - full;
		//
		//						for (int i = 0; i < full + partial; i++)
		//						{
		//							ClientUtils.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
		//						}
		//
		//						air -= 300;
		//						top -= 8;
		//					}
		//				}
		//
		//				GlStateManager.color(1.0F, 1.0F, 1.0F);
		//				//GlStateManager.disableBlend();
		//				GlStateManager.popMatrix();
		//			}
		//		}
		//	}
		private final Set<UUID> setIsOxygenPowered = new HashSet<>();

		@SubscribeEvent
		public void handleLivingUpdate(CyberwareUpdateEvent event)
		{
			LivingEntity entityLivingBase = event.getEntity();
			ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

			var oxygenItem = (ItemLungsUpgrade) Lungs.OXYGEN.get();
			ItemStack itemStackCompressedAir = cyberwareUserData.getCyberware(oxygenItem.getDefaultInstance());
			if (!itemStackCompressedAir.isEmpty())
			{
				int air = oxygenItem.getAir(itemStackCompressedAir);
				if (entityLivingBase.getAirSupply() < 300 && air > 0)
				{
					int toAdd = Math.min(300 - entityLivingBase.getAirSupply(), air);
					entityLivingBase.setAirSupply(entityLivingBase.getAirSupply() + toAdd);
					CyberwareAPI.getCyberwareNBT(itemStackCompressedAir).putInt("air", air - toAdd);
				} else if (entityLivingBase.getAirSupply() == 300 && air < 900)
				{
					CyberwareAPI.getCyberwareNBT(itemStackCompressedAir).putInt("air", air + 1);
				}
			}

			var hyperItem = (ItemLungsUpgrade) Lungs.HYPER_OXYGENATION.get();
			ItemStack itemStackHyperOxygenationBoost =
				cyberwareUserData.getCyberware(hyperItem.getDefaultInstance());
			if (!itemStackHyperOxygenationBoost.isEmpty())
			{
				if ((entityLivingBase.isSprinting() || entityLivingBase instanceof Mob)
					&& !entityLivingBase.isInWater()
					&& entityLivingBase.isOnGround())
				{
					boolean wasPowered = setIsOxygenPowered.contains(entityLivingBase.getUUID());
					boolean isPowered = entityLivingBase.tickCount % 20 == 0
						? cyberwareUserData.usePower(
						itemStackHyperOxygenationBoost,
						hyperItem.getPowerConsumption(itemStackHyperOxygenationBoost)
					)
						: wasPowered;
					if (isPowered)
					{
						if (entityLivingBase.getDeltaMovement().horizontalDistance() > 0.0F)
						{
							// increase maximum horizontal motion
							float boost = 0.21F * itemStackHyperOxygenationBoost.getCount();
							// TODO: this correct?
							entityLivingBase.moveRelative(
								(float) (entityLivingBase.getDeltaMovement().horizontalDistance() * boost),
								entityLivingBase.getForward()
							);
						}

						if (!wasPowered)
						{
							setIsOxygenPowered.add(entityLivingBase.getUUID());
						}
					} else if (entityLivingBase.tickCount % 20 == 0)
					{
						setIsOxygenPowered.remove(entityLivingBase.getUUID());
					}
				}
			}
		}
	}
}
