package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.CyberwareContent.ZombieItem;
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.config.StartingStacksConfig;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.CyberwareSyncPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.PlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.ValueType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CyberwareDataHandler
{
	public static final CyberwareDataHandler INSTANCE = new CyberwareDataHandler();
	public static final String KEEP_WARE_GAMERULE = "cyberware_keepCyberware";
	public static final String DROP_WARE_GAMERULE = "cyberware_dropCyberware";

	@SubscribeEvent
	public void onEntityConstructed(EntityEvent.EntityConstructing event)
	{
		if (event.getEntity() instanceof LivingEntity)
		{
			LivingEntity entityLivingBase = (LivingEntity) event.getEntity();
			entityLivingBase.getAttributeMap().registerAttribute(CyberwareAPI.TOLERANCE_ATTR);
		}
	}

	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event)
	{
		GameRules rules = event.getLevel().getGameRules();
		if (!rules.hasRule(KEEP_WARE_GAMERULE))
		{
			rules.addGameRule(KEEP_WARE_GAMERULE, Boolean.toString(CyberwareConfig.INSTANCE.DEFAULT_KEEP.get()),
				ValueType.BOOLEAN_VALUE
			);
		}
		if (!rules.hasRule(DROP_WARE_GAMERULE))
		{
			rules.addGameRule(DROP_WARE_GAMERULE, Boolean.toString(CyberwareConfig.INSTANCE.DEFAULT_DROP.get()),
				ValueType.BOOLEAN_VALUE
			);
		}
	}

	@SubscribeEvent
	public void attachCyberwareData(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player)
		{
			event.addCapability(CyberwareUserDataImpl.Provider.NAME, new CyberwareUserDataImpl.Provider());
		}
	}

	@SubscribeEvent
	public void playerDeathEvent(PlayerEvent.Clone event)
	{
		Player PlayerLiving = event.getEntity();
		Player PlayerDead = event.getOriginal();
		if (event.isWasDeath())
		{
			if (PlayerLiving.level.getWorldInfo().getGameRulesInstance().getBoolean(KEEP_WARE_GAMERULE))
			{
				ICyberwareUserData cyberwareUserDataDead = CyberwareAPI.getCapabilityOrNull(PlayerDead);
				ICyberwareUserData cyberwareUserDataLiving = CyberwareAPI.getCapabilityOrNull(PlayerLiving);
				if (cyberwareUserDataDead != null && cyberwareUserDataLiving != null)
				{
					cyberwareUserDataLiving.deserializeNBT(cyberwareUserDataDead.serializeNBT());
				}
			}
		} else
		{
			ICyberwareUserData cyberwareUserDataDead = CyberwareAPI.getCapabilityOrNull(PlayerDead);
			ICyberwareUserData cyberwareUserDataLiving = CyberwareAPI.getCapabilityOrNull(PlayerLiving);
			if (cyberwareUserDataDead != null && cyberwareUserDataLiving != null)
			{
				cyberwareUserDataLiving.deserializeNBT(cyberwareUserDataDead.serializeNBT());
			}
		}
	}

	@SubscribeEvent
	public void handleCyberzombieDrops(LivingDropsEvent event)
	{
		LivingEntity entityLivingBase = event.getEntity();
		if (entityLivingBase instanceof Player Player && !entityLivingBase.level.isClientSide())
		{
			if ((Player.level.getLevelData().getGameRules().getBoolean(DROP_WARE_GAMERULE)
				&& !Player.level.getLevelData().getGameRules().getBoolean(KEEP_WARE_GAMERULE))
				|| (Player.level.getLevelData().getGameRules().getBoolean(KEEP_WARE_GAMERULE)
				&& shouldDropWare(event.getSource())))
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(Player);
				if (cyberwareUserData != null)
				{
					for (EnumSlot slot : EnumSlot.values())
					{
						NonNullList<ItemStack> nnlInstalled = cyberwareUserData.getInstalledCyberware(slot);
						NonNullList<ItemStack> nnlDefaults = NonNullList.create();
						for (ItemStack itemStackDefault :
							StartingStacksConfig.getStartingItems(EnumSlot.values()[slot.ordinal()]))
						{
							nnlDefaults.add(itemStackDefault.copy());
						}
						for (ItemStack itemStackInstalled : nnlInstalled)
						{
							if (!itemStackInstalled.isEmpty())
							{
								ItemStack itemStackToDrop = itemStackInstalled.copy();
								boolean found = false;
								for (ItemStack itemStackDefault : nnlDefaults)
								{
									if (CyberwareAPI.areCyberwareStacksEqual(itemStackDefault, itemStackToDrop))
									{
										if (itemStackToDrop.getCount() > itemStackDefault.getCount())
										{
											itemStackToDrop.shrink(itemStackDefault.getCount());
										} else
										{
											found = true;
										}
									}
								}

								if (!found
									&& Player.level.random.nextFloat() < CyberwareConfig.INSTANCE.DROP_CHANCE.get() / 100F)
								{
									ItemEntity entityItem = new ItemEntity(Player.level, Player.posX, Player.posY,
										Player.posZ, itemStackToDrop
									);
									event.getDrops().add(entityItem);
								}
							}
						}
					}
					cyberwareUserData.resetWare(Player);
				}
			}
		}
	}

	private boolean shouldDropWare(DamageSource source)
	{
		if (source == EssentialsMissingHandler.noessence) return true;
		if (source == EssentialsMissingHandler.heartless) return true;
		if (source == EssentialsMissingHandler.brainless) return true;
		if (source == EssentialsMissingHandler.nomuscles) return true;
		if (source == EssentialsMissingHandler.spineless) return true;

		return false;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleCZSpawn(LivingSpawnEvent.SpecialSpawn event)
	{
		LivingEntity entityLiving = event.getEntity();

		if (entityLiving instanceof Piglin
			|| !(entityLiving instanceof Zombie))
		{
			final ResourceLocation resourceLocation = EntityList.getKey(entityLiving);
			if (resourceLocation == null
				|| !resourceLocation.getPath().contains("ombie"))
			{
				return;
			}
		}

		if (CyberwareConfig.INSTANCE.MOBS_ENABLE_CYBER_ZOMBIES.get()
			&& !(entityLiving instanceof EntityCyberZombie)
			&& (!CyberwareConfig.INSTANCE.MOBS_APPLY_DIMENSION_TO_BEACON.get()
			|| isValidDimension(event.getLevel())))
		{
			var pos = entityLiving.position();
			int tier = TileEntityBeacon.isInRange(entityLiving.level, pos.x, pos.y, pos.z);
			if (tier > 0)
			{
				float chance = tier == 2 ? LibConstants.BEACON_CHANCE
					: tier == 1 ? LibConstants.BEACON_CHANCE_INTERNAL
						: LibConstants.LARGE_BEACON_CHANCE;
				if ((event.getLevel().getRandom().nextFloat() < (chance / 100F)))
				{
					EntityCyberZombie entityCyberZombie = new EntityCyberZombie(event.getLevel());
					if (event.getLevel().getRandom().nextFloat() < (LibConstants.BEACON_BRUTE_CHANCE / 100F))
					{
						entityCyberZombie.setBrute();
					}
					entityCyberZombie.setLocationAndAngles(entityLiving.posX, entityLiving.posY, entityLiving.posZ,
						entityLiving.rotationYaw, entityLiving.rotationPitch
					);
					entityCyberZombie.onInitialSpawn(event.getLevel().getDifficultyForLocation(entityCyberZombie.getPosition()), null);

					for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
					{
						if (entityCyberZombie.getItemStackFromSlot(slot).isEmpty())
						{
							entityCyberZombie.setItemStackToSlot(slot, entityLiving.getItemStackFromSlot(slot));
							// @TODO: transfer drop chance, see Halloween in Vanilla
						}
					}
					event.getLevel().addFreshEntity(entityCyberZombie);
					entityLiving.deathTime = 19;
					entityLiving.setHealth(0F);

					// continue processing to get a chance for clothing
					entityLiving = entityCyberZombie;
				}
			}
		}

		if (CyberwareConfig.INSTANCE.ENABLE_CLOTHES.get()
			&& CyberwareConfig.INSTANCE.MOBS_ADD_CLOTHES.get())
		{
			if (entityLiving.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()
				&& entityLiving.level.getRandom().nextFloat() < LibConstants.ZOMBIE_SHADES_CHANCE / 100F)
			{
				if (entityLiving.level.getRandom().nextBoolean())
				{
					entityLiving.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(CyberwareContent.shades));
				} else
				{
					entityLiving.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(CyberwareContent.shades2));
				}

				entityLiving.setDropChance(
					EntityEquipmentSlot.HEAD,
					CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F
				);
			}

			float chestRand = entityLiving.level.getRandom().nextFloat();

			if (entityLiving.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()
				&& chestRand < LibConstants.ZOMBIE_TRENCH_CHANCE / 100F)
			{
				ItemStack stack = new ItemStack(CyberwareContent.trenchCoat);
				int rand = entityLiving.level.getRandom().nextInt(3);
				if (rand == 0)
				{
					CyberwareContent.trenchCoat.setColor(stack, 0x664028);
				} else if (rand == 1)
				{
					CyberwareContent.trenchCoat.setColor(stack, 0xEAEAEA);
				}

				entityLiving.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);

				entityLiving.setDropChance(
					EntityEquipmentSlot.CHEST,
					CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F
				);
			} else if (entityLiving.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()
				&& chestRand - (LibConstants.ZOMBIE_TRENCH_CHANCE / 100F) < LibConstants.ZOMBIE_BIKER_CHANCE / 100F)
			{
				ItemStack stack = new ItemStack(CyberwareContent.jacket);

				entityLiving.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);

				entityLiving.setDropChance(
					EntityEquipmentSlot.CHEST,
					CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F
				);
			}
		}
	}

	public static void addRandomCyberware(EntityCyberZombie cyberZombie, boolean brute)
	{
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(cyberZombie);
		if (cyberwareUserData == null) return;

		NonNullList<NonNullList<ItemStack>> wares = NonNullList.create();

		for (EnumSlot slot : EnumSlot.values())
		{
			NonNullList<ItemStack> toAdd = cyberwareUserData.getInstalledCyberware(slot);
			toAdd.removeAll(Collections.singleton(ItemStack.EMPTY));
			wares.add(toAdd);
		}

		// Cyberzombies get all the power
		ItemStack battery = new ItemStack(CyberwareContent.creativeBattery);
		wares.get(CyberwareContent.creativeBattery.getSlot(battery).ordinal()).add(battery);

		int numberOfItemsToInstall = WeightedRandom.getRandomItem(
			cyberZombie.level.getRandom(),
			CyberwareContent.numItems
		).num;
		if (brute)
		{
			numberOfItemsToInstall += LibConstants.MORE_ITEMS_BRUTE;
		}

		List<ItemStack> installed = new ArrayList<>();

		List<ZombieItem> items = new ArrayList<>(CyberwareContent.zombieItems);
		for (int indexItem = 0; indexItem < numberOfItemsToInstall; indexItem++)
		{
			int tries = 0;
			ItemStack randomItem;
			ICyberware randomWare;

			// Ensure we get a unique item
			do
			{
				randomItem = WeightedRandom.getRandomItem(cyberZombie.level.random, items).orElseThrow().stack.copy();
				randomWare = CyberwareAPI.getCyberware(randomItem);
				randomItem.setCount(randomWare.installedStackSize(randomItem));
				tries++;
			}
			while (contains(wares.get(randomWare.getSlot(randomItem).ordinal()), randomItem) && tries < 10);

			if (tries < 10)
			{
				// Fulfill requirements
				NonNullList<NonNullList<ItemStack>> required = randomWare.required(randomItem);
				for (NonNullList<ItemStack> requiredCategory : required)
				{
					boolean found = false;
					for (ItemStack option : requiredCategory)
					{
						ICyberware optionWare = CyberwareAPI.getCyberware(option);
						option.setCount(optionWare.installedStackSize(option));
						if (contains(wares.get(optionWare.getSlot(option).ordinal()), option))
						{
							found = true;
							break;
						}
					}

					if (!found)
					{
						ItemStack req =
							requiredCategory.get(cyberZombie.getLevel().getRandom().nextInt(requiredCategory.size())).copy();
						ICyberware reqWare = CyberwareAPI.getCyberware(req);
						req.setCount(reqWare.installedStackSize(req));
						wares.get(reqWare.getSlot(req).ordinal()).add(req);
						installed.add(req);
						indexItem++;
					}
				}
				wares.get(randomWare.getSlot(randomItem).ordinal()).add(randomItem);
				installed.add(randomItem);
			}
		}
		
		/*
		Cyberware.logger.info(String.format("numberOfItemsToInstall is %s",
		                                    numberOfItemsToInstall));
		for (ItemStack stack : installed)
		{
			numberOfItemsToInstall(String.format("%d x %s",
			                                     stack.getCount(), stack.getTranslationKey() ));
		}
		*/

		for (EnumSlot slot : EnumSlot.values())
		{
			cyberwareUserData.setInstalledCyberware(cyberZombie, slot, wares.get(slot.ordinal()));
		}
		cyberwareUserData.updateCapacity();

		cyberZombie.setHealth(cyberZombie.getMaxHealth());
		cyberZombie.hasRandomWare = true;

		CyberwareAPI.updateData(cyberZombie);
	}

	private static boolean contains(NonNullList<ItemStack> nnlHaystack, ItemStack needle)
	{
		for (ItemStack check : nnlHaystack)
		{
			if (!check.isEmpty()
				&& !needle.isEmpty()
				&& check.getItem() == needle.getItem()
				&& CyberwareItemMetadata.identical(check, needle))
			{
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPotentialSpawns(@Nonnull LevelEvent.PotentialSpawns event)
	{
		if (event.getMobCategory() != MobCategory.MONSTER) return;

		if (!CyberwareConfig.INSTANCE.MOBS_APPLY_DIMENSION_TO_SPAWNING.get()) return;

		if (isValidDimension(event.getLevel())) return;

		List<MobSpawnSettings.SpawnerData> spawnListEntriesToRemove = new ArrayList<>(4);
		for (MobSpawnSettings.SpawnerData spawnListEntry : event.getSpawnerDataList())
		{
			// TODO
			if (spawnListEntry.type.equals(EntityCyberZombie.class))
			{
				spawnListEntriesToRemove.add(spawnListEntry);
			}
		}
		event.getSpawnerDataList().removeAll(spawnListEntriesToRemove);
	}

	public boolean isValidDimension(@Nonnull LevelAccessor level)
	{
		// TODO
		boolean isListed = CyberwareConfig.INSTANCE.MOBS_DIMENSION_IDS.get().contains(level.dimension());
		return (CyberwareConfig.INSTANCE.MOBS_IS_DIMENSION_BLACKLIST.get() && !isListed)
			|| (!CyberwareConfig.INSTANCE.MOBS_IS_DIMENSION_BLACKLIST.get() && isListed);
	}

	@SubscribeEvent
	public void syncCyberwareData(EntityJoinLevelEvent event)
	{
		if (!event.getLevel().isClientSide())
		{
			Entity entity = event.getEntity();
			if (entity instanceof Player)
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entity);
				if (cyberwareUserData != null)
				{
					CompoundTag tagCompound = cyberwareUserData.serializeNBT();
					CyberwarePacketHandler.INSTANCE.sendTo(
						new CyberwareSyncPacket(tagCompound, entity.getId()),
						(PlayerMP) entity
					);
				}
			}
		}
	}

	@SubscribeEvent
	public void startTrackingEvent(StartTracking event)
	{
		Player entityPlayer = event.getEntity();
		Entity entityTarget = event.getTarget();

		if (!entityTarget.level.isClientSide())
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityTarget);
			if (cyberwareUserData != null)
			{
				CompoundTag tagCompound = cyberwareUserData.serializeNBT();
				CyberwarePacketHandler.INSTANCE.send(
					PacketDistributor.PLAYER.with(() -> entityPlayer),
					new CyberwareSyncPacket(tagCompound, entityTarget.getId())
				);
				//				CyberwarePacketHandler.INSTANCE.sendTo(new CyberwareSyncPacket(tagCompound,
				//				entityTarget.getId()), (ServerPlayer) Player);
			}
		}
	}
}
