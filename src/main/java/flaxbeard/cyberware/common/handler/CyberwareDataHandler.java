package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.BodyRegionEnum;
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.config.StartingStacksConfig;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;
import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import flaxbeard.cyberware.common.item.ItemCreativeBattery;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.CyberwareItemMetadata;
import flaxbeard.cyberware.common.misc.ZombieItem;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.CyberwareSyncPacket;
import flaxbeard.cyberware.common.registry.CWAttributes;
import flaxbeard.cyberware.common.registry.CWGameRules;
import flaxbeard.cyberware.common.registry.items.Armors;
import flaxbeard.cyberware.common.registry.items.LowerOrgans;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CyberwareDataHandler
{
	public static final CyberwareDataHandler INSTANCE = new CyberwareDataHandler();

	@SubscribeEvent
	public void worldLoad(LevelEvent.Load event)
	{
		// used to load game rules
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
			GameRules rules = Objects.requireNonNull(PlayerLiving.getLevel().getServer()).getGameRules();
			if (rules.getBoolean(CWGameRules.KEEP_WARE_GAMERULE))
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
		if (entityLivingBase instanceof Player player && !entityLivingBase.level.isClientSide())
		{
			var rules = player.level.getLevelData().getGameRules();
			if ((rules.getBoolean(CWGameRules.DROP_WARE_GAMERULE) &&
				!rules.getBoolean(CWGameRules.KEEP_WARE_GAMERULE)) ||
				(rules.getBoolean(CWGameRules.KEEP_WARE_GAMERULE) && shouldDropWare(event.getSource())))
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(player);
				if (cyberwareUserData != null)
				{
					for (BodyRegionEnum slot : BodyRegionEnum.values())
					{
						NonNullList<ItemStack> nnlInstalled = cyberwareUserData.getInstalledCyberware(slot);
						NonNullList<ItemStack> nnlDefaults = NonNullList.create();
						for (ItemStack itemStackDefault :
							StartingStacksConfig.getStartingItems(BodyRegionEnum.values()[slot.ordinal()]))
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
									&& player.level.random.nextFloat() < CyberwareConfig.INSTANCE.DROP_CHANCE.get() / 100F)
								{
									var pos = player.position();
									ItemEntity entityItem = new ItemEntity(player.level, pos.x, pos.y,
										pos.z, itemStackToDrop
									);
									event.getDrops().add(entityItem);
								}
							}
						}
					}
					cyberwareUserData.resetWare(player);
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
		Mob entityLiving = event.getEntity();

		if (!(entityLiving instanceof Zombie))
		{
			final ResourceLocation resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entityLiving.getType());
			if (resourceLocation == null ||
				!resourceLocation.getPath().toLowerCase().contains("zombie"))
			{
				return;
			}
		}

		Cyberware.logger.debug("what the fuck: " + CyberwareConfig.INSTANCE.MOBS_ENABLE_CYBER_ZOMBIES.get());

		if (
			CyberwareConfig.INSTANCE.MOBS_ENABLE_CYBER_ZOMBIES.get()
			&& !(entityLiving instanceof EntityCyberZombie)
			&& (!CyberwareConfig.INSTANCE.MOBS_APPLY_DIMENSION_TO_BEACON.get()
			|| isValidDimension(event.getLevel())))
		{
			var posLiving = entityLiving.position();
			int tier = TileEntityBeacon.isInRange(entityLiving.level, posLiving.x, posLiving.y, posLiving.z);
			if (tier > 0)
			{
				float chance = tier == 2 ? LibConstants.BEACON_CHANCE
					: tier == 1 ? LibConstants.BEACON_CHANCE_INTERNAL
						: LibConstants.LARGE_BEACON_CHANCE;
				if ((event.getLevel().getRandom().nextFloat() < (chance / 100F)))
				{
					EntityCyberZombie entityCyberZombie = new EntityCyberZombie((Level) event.getLevel());
					if (event.getLevel().getRandom().nextFloat() < (LibConstants.BEACON_BRUTE_CHANCE / 100F))
					{
						entityCyberZombie.setBrute();
					}

					entityCyberZombie.setPos(entityLiving.position());
					entityCyberZombie.setXRot(entityLiving.getXRot());
					entityCyberZombie.setYRot(entityLiving.getYRot());

					var pos = entityCyberZombie.getPosition(1.0F);
					entityCyberZombie.finalizeSpawn(
						(ServerLevelAccessor) event.getLevel(),
						event.getLevel().getCurrentDifficultyAt(new BlockPos(pos.x, pos.y, pos.z)),
						MobSpawnType.NATURAL,
						null,
						null
					);

					ForgeEventFactory.doSpecialSpawn(
						entityCyberZombie, event.getLevel(), (float) pos.x, (float) pos.y, (float) pos.z,
						null, MobSpawnType.NATURAL
					);

					// TODO apparently must call net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn

					for (EquipmentSlot slot : EquipmentSlot.values())
					{
						if (entityCyberZombie.getItemBySlot(slot).isEmpty())
						{
							entityCyberZombie.setItemSlot(slot, entityLiving.getItemBySlot(slot));
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
			if (entityLiving.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
				&& entityLiving.level.getRandom().nextFloat() < LibConstants.ZOMBIE_SHADES_CHANCE / 100F)
			{
				if (entityLiving.level.getRandom().nextBoolean())
				{
					entityLiving.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Armors.SHADES.get()));
				} else
				{
					entityLiving.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Armors.SHADES2.get()));
				}

				entityLiving.setDropChance(
					EquipmentSlot.HEAD,
					(float) (CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F)
				);
			}

			float chestRand = entityLiving.level.getRandom().nextFloat();

			if (entityLiving.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
				&& chestRand < LibConstants.ZOMBIE_TRENCH_CHANCE / 100F)
			{
				var trenchCoat = (ItemArmorCyberware) Armors.TRENCH_COAT.get();
				ItemStack stack = new ItemStack(trenchCoat);
				int rand = entityLiving.level.getRandom().nextInt(3);
				if (rand == 0)
				{
					trenchCoat.setColor(stack, 0x664028);
				} else if (rand == 1)
				{
					trenchCoat.setColor(stack, 0xEAEAEA);
				}

				entityLiving.setItemSlot(EquipmentSlot.CHEST, stack);

				entityLiving.setDropChance(
					EquipmentSlot.CHEST,
					(float) (CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F)
				);
			} else if (entityLiving.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
				&& chestRand - (LibConstants.ZOMBIE_TRENCH_CHANCE / 100F) < LibConstants.ZOMBIE_BIKER_CHANCE / 100F)
			{
				ItemStack stack = new ItemStack(Armors.JACKET.get());

				entityLiving.setItemSlot(EquipmentSlot.CHEST, stack);

				entityLiving.setDropChance(
					EquipmentSlot.CHEST,
					(float) (CyberwareConfig.INSTANCE.MOBS_CLOTH_DROP_RARITY.get() / 100F)
				);
			}
		}
	}

	public static void addRandomCyberware(EntityCyberZombie cyberZombie, boolean brute)
	{
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(cyberZombie);
		if (cyberwareUserData == null) return;

		NonNullList<NonNullList<ItemStack>> wares = NonNullList.create();

		for (BodyRegionEnum slot : BodyRegionEnum.values())
		{
			NonNullList<ItemStack> toAdd = cyberwareUserData.getInstalledCyberware(slot);
			toAdd.removeAll(Collections.singleton(ItemStack.EMPTY));
			wares.add(toAdd);
		}

		// Cyberzombies get all the power
		var creativeBat = (ItemCreativeBattery) LowerOrgans.BATTERY_CREATIVE.get();
		ItemStack battery = new ItemStack(creativeBat);
		wares.get(creativeBat.getSlot(battery).ordinal()).add(battery);

		// TODO: seems very wrong, used to be .num
		int numberOfItemsToInstall = WeightedRandom.getRandomItem(
			cyberZombie.level.getRandom(),
			ZombieItem.entries
		).orElseThrow().getWeight().asInt();

		if (brute)
		{
			numberOfItemsToInstall += LibConstants.MORE_ITEMS_BRUTE;
		}

		List<ItemStack> installed = new ArrayList<>();

		List<ZombieItem> items = new ArrayList<>(ZombieItem.entries.size());
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
				randomItem.setCount(randomWare.maximumStackSize(randomItem));
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
						option.setCount(optionWare.maximumStackSize(option));
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
						req.setCount(reqWare.maximumStackSize(req));
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

		for (BodyRegionEnum slot : BodyRegionEnum.values())
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
		// TODO: seems wrong
		var level2 = (Level) level;
		boolean isListed = CyberwareConfig.INSTANCE.MOBS_DIMENSION_IDS.get().contains(level2.dimension());
		return (CyberwareConfig.INSTANCE.MOBS_IS_DIMENSION_BLACKLIST.get() && !isListed)
			|| (!CyberwareConfig.INSTANCE.MOBS_IS_DIMENSION_BLACKLIST.get() && isListed);
	}

	@SubscribeEvent
	public void syncCyberwareData(EntityJoinLevelEvent event)
	{
		if (!event.getLevel().isClientSide())
		{
			if (event.getEntity() instanceof ServerPlayer player)
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(player);
				if (cyberwareUserData != null)
				{
					CompoundTag tagCompound = cyberwareUserData.serializeNBT();

					CyberwarePacketHandler.INSTANCE.send(
						PacketDistributor.PLAYER.with(() -> player),
						new CyberwareSyncPacket(tagCompound, player.getId())
					);
				}
			}
		}
	}

	@SubscribeEvent
	public void startTrackingEvent(StartTracking event)
	{
		ServerPlayer entityPlayer = (ServerPlayer) event.getEntity();
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
			}
		}
	}
}
