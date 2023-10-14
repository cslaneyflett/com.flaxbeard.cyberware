package flaxbeard.cyberware.common.entity;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.item.ICyberware.BodyRegionEnum;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.handler.CyberwareDataHandler;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.registry.CWTags;
import flaxbeard.cyberware.common.registry.items.LowerOrgans;
import flaxbeard.cyberware.common.registry.items.Misc;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EntityCyberZombie extends Zombie
{
	private static final EntityDataAccessor<Integer> CYBER_VARIANT =
		SynchedEntityData.defineId(EntityCyberZombie.class, EntityDataSerializers.INT);
	public boolean hasRandomWare;
	private final CyberwareUserDataImpl cyberware;
	private final LazyOptional<CyberwareUserDataImpl> lazyCyberware;

	public EntityCyberZombie(Level worldIn)
	{
		super(worldIn);
		cyberware = new CyberwareUserDataImpl();
		lazyCyberware = LazyOptional.of(() -> cyberware);
		hasRandomWare = false;
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		entityData.define(CYBER_VARIANT, 0);
	}

	@Override
	public void tick()
	{
		if (!hasRandomWare &&
			!level.isClientSide()
		)
		{
			if (!isBrute()
				&& level.random.nextFloat() < (LibConstants.NATURAL_BRUTE_CHANCE / 100F))
			{
				setBrute();
			}

			CyberwareDataHandler.addRandomCyberware(this, isBrute());
			if (isBrute())
			{
				// TODO: permanent or transient modifier?
				Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH))
					.addPermanentModifier(new AttributeModifier("Brute Bonus", 6D, AttributeModifier.Operation.ADDITION));
				Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE))
					.addPermanentModifier(new AttributeModifier("Brute Bonus", 1D, AttributeModifier.Operation.ADDITION));
			}
			setHealth(getMaxHealth());
			hasRandomWare = true;
		}

		if (isBrute() &&
			getBbHeight() != (1.95F * 1.2F)
		)
		{
			setSizeNormal(0.6F * 1.2F, 1.95F * 1.2F);
		}

		super.tick();
	}

	// TODO: refactor, no width/height, only BB, half updated this already
	protected void setSizeNormal(float width, float height)
	{
		if (width != this.getBbWidth()
			|| height != this.getBbHeight())
		{
			float widthPrevious = this.getBbWidth();
			//			this.width = width;
			//			this.height = height;

			AABB axisalignedbb = getBoundingBox();
			setBoundingBox(new AABB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
				axisalignedbb.minX + width, axisalignedbb.minY + height, axisalignedbb.minZ + width
			));

			if (this.getBbWidth() > widthPrevious
				&& !firstTick
				&& !level.isClientSide())
			{
				move(MoverType.SELF, new Vec3(widthPrevious - width, 0.0D, widthPrevious - width));
			}
		}
	}

	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag tagCompound)
	{
		super.readAdditionalSaveData(tagCompound);

		tagCompound.putBoolean("hasRandomWare", hasRandomWare);
		tagCompound.putBoolean("brute", isBrute());

		if (hasRandomWare)
		{
			CompoundTag tagCompoundCyberware = cyberware.serializeNBT();
			tagCompound.put("ware", tagCompoundCyberware);
		}
	}

	@Override
	public void load(@Nonnull CompoundTag tagCompound)
	{
		super.load(tagCompound);

		boolean brute = tagCompound.getBoolean("brute");
		if (brute)
		{
			setBrute();
		}
		hasRandomWare = tagCompound.getBoolean("hasRandomWare");
		if (tagCompound.contains("ware"))
		{
			cyberware.deserializeNBT(tagCompound.getCompound("ware"));
		}
	}

	@Override
	public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
													  final @Nullable Direction facing)
	{
		if (capability == CyberwareAPI.CYBERWARE_CAPABILITY)
		{
			return lazyCyberware.cast();
		}

		return super.getCapability(capability, facing);
	}

	//	@Override
	//	public boolean hasCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<?> capability,
	//								 Direction facing)
	//	{
	//		return capability == CyberwareAPI.CYBERWARE_CAPABILITY
	//				|| super.hasCapability(capability, facing);
	//	}

	@Override
	protected void dropCustomDeathLoot(@Nonnull DamageSource pSource, int pLooting, boolean pRecentlyHit)
	{
		super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);

		if (CyberwareConfig.INSTANCE.ENABLE_KATANA.get()
			&& CyberwareConfig.INSTANCE.MOBS_ADD_CLOTHES.get()
			&& !getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
			&& getItemBySlot(EquipmentSlot.MAINHAND).is(Misc.KATANA.get()))
		{
			ItemStack itemstack = getItemBySlot(EquipmentSlot.MAINHAND).copy();
			if (itemstack.isDamageableItem())
			{
				int i = Math.max(itemstack.getMaxDamage() - 25, 1);
				int j = itemstack.getMaxDamage() - random.nextInt(random.nextInt(i) + 1);

				if (j > i)
				{
					j = i;
				}

				if (j < 1)
				{
					j = 1;
				}

				itemstack.setDamageValue(j);
			}

			// TODO
			this.spawnAtLocation(itemstack, 0.0F);
		}

		if (hasRandomWare)
		{
			double rarity = Math.min(
				100.0D,
				CyberwareConfig.INSTANCE.MOBS_CYBER_ZOMBIE_DROP_RARITY.get() + pLooting * 5.0F
			);
			if (level.getRandom().nextFloat() < (rarity / 100.0F))
			{
				List<ItemStack> allWares = new ArrayList<>();
				for (BodyRegionEnum slot : BodyRegionEnum.values())
				{
					NonNullList<ItemStack> nnlInstalled = cyberware.getInstalledCyberware(slot);
					for (ItemStack stack : nnlInstalled)
					{
						if (!stack.isEmpty())
						{
							allWares.add(stack);
						}
					}
				}

				allWares.removeAll(Collections.singleton(ItemStack.EMPTY));

				// Sanity check for corrupted NBT
				if (allWares.isEmpty())
				{
					Cyberware.logger.error(String.format("Invalid cyberzombie with hasRandomWare %s with actually no " +
							"implants: %s",
						hasRandomWare, this
					));
					return;
				}

				// TODO: body part tag
				ItemStack drop = ItemStack.EMPTY;
				int count = 0;
				while (count < 50
					&& (drop.isEmpty()
					|| drop.is(LowerOrgans.BATTERY_CREATIVE.get())
					|| drop.is(CWTags.BODY_PARTS)))
				{
					int random = level.getRandom().nextInt(allWares.size());
					drop = allWares.get(random).copy();
					drop = CyberwareAPI.sanitize(drop);
					drop = CyberwareAPI.getCyberware(drop).setQuality(drop, CyberwareAPI.QUALITY_SCAVENGED);
					drop.setCount(1);
					count++;
				}

				if (count < 50)
				{
					this.spawnAtLocation(drop, 0.0F);
				}
			}
		}
	}

	@Override
	protected void populateDefaultEquipmentSlots(@Nonnull RandomSource pRandom, @Nonnull DifficultyInstance pDifficulty)
	{
		super.populateDefaultEquipmentSlots(pRandom, pDifficulty);

		if (CyberwareConfig.INSTANCE.ENABLE_KATANA.get()
			&& CyberwareConfig.INSTANCE.MOBS_ADD_CLOTHES.get()
			&& !getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
			&& getItemBySlot(EquipmentSlot.MAINHAND).getItem() == Items.IRON_SWORD)
		{
			setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Misc.KATANA.get()));
			setDropChance(EquipmentSlot.MAINHAND, 0F);
		}
	}

	public boolean isBrute()
	{
		return entityData.get(CYBER_VARIANT) == 1;
	}

	public void setBrute()
	{
		setBaby(false);
		entityData.set(CYBER_VARIANT, 1);
	}
}
