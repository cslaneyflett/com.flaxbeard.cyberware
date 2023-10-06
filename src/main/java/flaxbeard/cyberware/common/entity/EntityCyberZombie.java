package flaxbeard.cyberware.common.entity;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.handler.CyberwareDataHandler;
import flaxbeard.cyberware.common.lib.LibConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.level.DifficultyInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
	private CyberwareUserDataImpl cyberware;
	private final LazyOptional<CyberwareUserDataImpl> lazyCyberware = LazyOptional.of(() -> cyberware);

	public EntityCyberZombie(Level worldIn)
	{
		super(worldIn);
		cyberware = new CyberwareUserDataImpl();
		hasRandomWare = false;
	}

	protected void entityInit()
	{
		super.entityInit();
		entityData.define(CYBER_VARIANT, 0);
	}

	@Override
	public void onLivingUpdate()
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
				Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(new AttributeModifier("Brute Bonus", 6D, AttributeModifier.Operation.ADDITION));
				Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(new AttributeModifier("Brute Bonus", 1D, AttributeModifier.Operation.ADDITION));
			}
			setHealth(getMaxHealth());
			hasRandomWare = true;
		}

		if (isBrute() &&
			height != (1.95F * 1.2F)
		)
		{
			setSizeNormal(0.6F * 1.2F, 1.95F * 1.2F);
		}
		super.onLivingUpdate();
	}

	// TODO: refactor, no width/height, only BB, half updated this already
	protected void setSizeNormal(float width, float height)
	{
		if (width != this.width
			|| height != this.height)
		{
			float widthPrevious = this.getBbWidth();
			this.width = width;
			this.height = height;
			AABB axisalignedbb = getBoundingBox();
			setBoundingBox(new AABB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
				axisalignedbb.minX + width, axisalignedbb.minY + height, axisalignedbb.minZ + width
			));

			if (this.getBbWidth() > widthPrevious
				&& !firstUpdate
				&& !level.isClientSide())
			{
				move(MoverType.SELF, widthPrevious - width, 0.0D, widthPrevious - width);
			}
		}
	}

	@Nonnull
	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);

		tagCompound.putBoolean("hasRandomWare", hasRandomWare);
		tagCompound.putBoolean("brute", isBrute());

		if (hasRandomWare)
		{
			CompoundTag tagCompoundCyberware = cyberware.serializeNBT();
			tagCompound.put("ware", tagCompoundCyberware);
		}
		return tagCompound;
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound)
	{
		super.readFromNBT(tagCompound);

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
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier)
	{
		super.dropEquipment(wasRecentlyHit, lootingModifier);

		if (CyberwareConfig.INSTANCE.ENABLE_KATANA.get()
			&& CyberwareConfig.INSTANCE.MOBS_ADD_CLOTHES.get()
			&& !getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
			&& getItemBySlot(EquipmentSlot.MAINHAND).getItem() == CyberwareContent.katana)
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
			entityDropItem(itemstack, 0.0F);
		}

		if (hasRandomWare)
		{
			float rarity = Math.min(
				100.0F,
				CyberwareConfig.INSTANCE.MOBS_CYBER_ZOMBIE_DROP_RARITY.get() + lootingModifier * 5.0F
			);
			if (level.rand.nextFloat() < (rarity / 100.0F))
			{
				List<ItemStack> allWares = new ArrayList<>();
				for (EnumSlot slot : EnumSlot.values())
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
				if (allWares.size() == 0)
				{
					Cyberware.logger.error(String.format("Invalid cyberzombie with hasRandomWare %s with actually no " +
							"implants: %s",
						hasRandomWare, this
					));
					return;
				}

				ItemStack drop = ItemStack.EMPTY;
				int count = 0;
				while (count < 50
					&& (drop.isEmpty()
					|| drop.getItem() == CyberwareContent.creativeBattery
					|| drop.getItem() == CyberwareContent.bodyPart))
				{
					int random = level.rand.nextInt(allWares.size());
					drop = allWares.get(random).copy();
					drop = CyberwareAPI.sanitize(drop);
					drop = CyberwareAPI.getCyberware(drop).setQuality(drop, CyberwareAPI.QUALITY_SCAVENGED);
					drop.setCount(1);
					count++;
				}

				if (count < 50)
				{
					entityDropItem(drop, 0.0F);
				}
			}
		}
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(@Nonnull DifficultyInstance difficulty)
	{
		super.setEquipmentBasedOnDifficulty(difficulty);

		if (CyberwareConfig.INSTANCE.ENABLE_KATANA.get()
			&& CyberwareConfig.INSTANCE.MOBS_ADD_CLOTHES.get()
			&& !getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()
			&& getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() == Items.IRON_SWORD)
		{
			setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(CyberwareContent.katana));
			setDropChance(EntityEquipmentSlot.MAINHAND, 0F);
		}
	}

	public boolean isBrute()
	{
		return entityData.get(CYBER_VARIANT) == 1;
	}

	public boolean setBrute()
	{
		setChild(false);
		entityData.set(CYBER_VARIANT, 1);

		return !hasRandomWare;
	}
}
