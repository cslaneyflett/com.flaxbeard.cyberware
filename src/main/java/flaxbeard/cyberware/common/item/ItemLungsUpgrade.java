package flaxbeard.cyberware.common.item;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.lib.LibConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemLungsUpgrade extends ItemCyberware
{
	private static final int META_COMPRESSED_OXYGEN = 0;
	private static final int META_HYPEROXYGENATION_BOOST = 1;

	public ItemLungsUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onDrawScreenPost(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() == ElementType.AIR)
		{
			Player entityPlayer = Minecraft.getInstance().player;
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData == null) return;

			ItemStack itemStackCompressedOxygen =
				cyberwareUserData.getCyberware(getCachedStack(META_COMPRESSED_OXYGEN));
			if (!itemStackCompressedOxygen.isEmpty()
				&& !entityPlayer.isCreative())
			{
				GlStateManager.pushMatrix();
				int air = getAir(itemStackCompressedOxygen);

				Minecraft.getInstance().getTextureManager().bindTexture(Gui.ICONS);

				ScaledResolution res = event.getResolution();
				GlStateManager.enableBlend();
				int left = res.getScaledWidth() / 2 + 91;
				int top = res.getScaledHeight() - 49 - 8;

				float r = 1.0F;
				float b = 1.0F;
				float g = 1.0F;

				if (entityPlayer.isInsideOfMaterial(Material.WATER))
				{
					while (air > 0)
					{
						r += 1.0F;
						b -= 0.25F;
						g += 0.25F;
						GlStateManager.color(r, g, b);
						int drawAir = Math.min(300, air);
						int full = Mth.ceil((drawAir - 2) * 10.0D / 300.0D);
						int partial = Mth.ceil(drawAir * 10.0D / 300.0D) - full;

						for (int i = 0; i < full + partial; i++)
						{
							ClientUtils.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
						}

						air -= 300;
						top -= 8;
					}
				}

				GlStateManager.color(1.0F, 1.0F, 1.0F);
				//GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	private Set<UUID> setIsOxygenPowered = new HashSet<>();

	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();

		ItemStack itemStackCompressedAir = cyberwareUserData.getCyberware(getCachedStack(META_COMPRESSED_OXYGEN));
		if (!itemStackCompressedAir.isEmpty())
		{
			int air = getAir(itemStackCompressedAir);
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

		ItemStack itemStackHyperoxygenationBoost =
			cyberwareUserData.getCyberware(getCachedStack(META_HYPEROXYGENATION_BOOST));
		if (!itemStackHyperoxygenationBoost.isEmpty())
		{
			if ((entityLivingBase.isSprinting() || entityLivingBase instanceof Mob)
				&& !entityLivingBase.isInWater()
				&& entityLivingBase.isOnGround())
			{
				boolean wasPowered = setIsOxygenPowered.contains(entityLivingBase.getUUID());
				boolean isPowered = entityLivingBase.tickCount % 20 == 0
					? cyberwareUserData.usePower(
					itemStackHyperoxygenationBoost,
					getPowerConsumption(itemStackHyperoxygenationBoost)
				)
					: wasPowered;
				if (isPowered)
				{
					if (Math.abs(entityLivingBase.moveStrafing) + Math.abs(entityLivingBase.moveForward) > 0.0F
						&& Math.abs(entityLivingBase.motionX) + Math.abs(entityLivingBase.motionZ) > 0.0F)
					{
						// increase maximum horizontal motion
						float boost = 0.21F * itemStackHyperoxygenationBoost.getCount();
						entityLivingBase.moveRelative(entityLivingBase.moveStrafing * boost, 0.0F,
							entityLivingBase.moveForward * boost, 0.075F
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

	@Override
	public int installedStackSize(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_HYPEROXYGENATION_BOOST ? 3 : 1;
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
	public int getPowerConsumption(ItemStack stack)
	{
		return CyberwareItemMetadata.get(stack) == META_HYPEROXYGENATION_BOOST ?
			LibConstants.HYPEROXYGENATION_CONSUMPTION * stack.getCount() : 0;
	}

	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (CyberwareItemMetadata.get(stack) == META_HYPEROXYGENATION_BOOST)
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
}
