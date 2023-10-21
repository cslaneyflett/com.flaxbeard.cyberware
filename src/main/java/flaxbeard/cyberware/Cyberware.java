package flaxbeard.cyberware;

import com.mojang.logging.LogUtils;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.client.KeyBinds;
import flaxbeard.cyberware.common.config.CyberwareConfig;
import flaxbeard.cyberware.common.config.StartingStacksConfig;
import flaxbeard.cyberware.common.handler.*;
import flaxbeard.cyberware.common.item.*;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.registry.*;
import flaxbeard.cyberware.common.registry.items.Armors;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Cyberware.MODID)
public class Cyberware
{
	public static final String MODID = "cyberware";
	public static final Logger logger = LogUtils.getLogger();

	//	public static CreativeModeTab creativeTab = new TabCyberware(MODID);

	public Cyberware()
	{
		logger.warn("CYBERWARE-INTERNAL: boot");
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::onAttributeModification);

		CWBlocks.BLOCKS.register(modEventBus);
		CWBlockItems.ITEMS.register(modEventBus); // its separate because lets me do data gen
		CWBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
		CWMobEffects.MOB_EFFECTS.register(modEventBus);
		CWRecipes.RECIPE_TYPES.register(modEventBus);
		CWAttributes.ATTRIBUTES.register(modEventBus);

		CWItems.init();
		CWItems.ITEMS.register(modEventBus);

		CyberwarePacketHandler.init();

		MinecraftForge.EVENT_BUS.register(this);
		// NetworkRegistry.INSTANCE.registerGuiHandler(Cyberware.INSTANCE, new GuiHandler());
		MinecraftForge.EVENT_BUS.register(CyberwareDataHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(CyberwareConfig.INSTANCE);
		MinecraftForge.EVENT_BUS.register(MiscHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(EssentialsMissingHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemCybereyes.ItemCybereyesEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemCyberlimb.ItemCyberlimbEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemArmUpgrade.ItemArmUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemLegUpgrade.ItemLegUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemBoneUpgrade.ItemBoneUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemFootUpgrade.ItemFootUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemHandUpgrade.ItemHandUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemSkinUpgrade.ItemSkinUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemLowerOrgansUpgrade.ItemLowerOrgansUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemLungsUpgrade.ItemLungsUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ItemMuscleUpgrade.ItemMuscleUpgradeEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(VanillaWares.SpiderEyeWare.VanillaWaresEventHandler.INSTANCE);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CyberwareConfig.INSTANCE_SPEC);

		logger.warn("CYBERWARE-INTERNAL: registration done");
	}

	// moved from data handler
	public void onAttributeModification(EntityAttributeModificationEvent event)
	{
		var attr = CWAttributes.TOLERANCE.get();

		for (EntityType<? extends LivingEntity> entityType : event.getTypes())
		{
			if (!event.has(entityType, attr))
			{
				event.add(entityType, attr);
			}
		}
	}

	private void commonSetup(final FMLCommonSetupEvent event)
	{
		logger.warn("CYBERWARE-INTERNAL: commonSetup");
		StartingStacksConfig.init();
		CyberwareAPI.linkCyberware(Items.SPIDER_EYE, new VanillaWares.SpiderEyeWare());
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class SharedModEvents
	{
		private SharedModEvents() {}

		@SubscribeEvent
		public void registerCaps(RegisterCapabilitiesEvent event)
		{
			event.register(ICyberwareUserData.class);
		}

		// This is shared because of physical sides.
		@SubscribeEvent
		public void onServerStarting(ServerStartingEvent event)
		{
			// event.registerServerCommand(new CommandClearCyberware());
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
	public static class ServerModEvents
	{
		private ServerModEvents() {}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		private ClientModEvents() {}

		//		@SubscribeEvent
		//		public void buildContents(CreativeModeTabEvent.Register event) {
		//			event.registerCreativeModeTab(new ResourceLocation(MOD_ID, "example"), builder ->
		//					// Set name of tab to display
		//					builder.title(Component.translatable("item_group." + MOD_ID + ".example"))
		//							// Set icon of creative tab
		//							.icon(() -> new ItemStack(ITEM.get()))
		//							// Add default items to tab
		//							.displayItems((params, output) -> {
		//								output.accept(ITEM.get());
		//								output.accept(BLOCK.get());
		//							})
		//			);
		//
		//			MinecraftForge.EVENT_BUS.register(new TabRegistry());
		//
		//			if (TabRegistry.getTabList().size() == 0) {
		//				TabRegistry.registerTab(new InventoryTabVanilla());
		//			}
		//
		//			TabRegistry.registerTab(new InventoryTabFineManipulators());
		//		}

		@SubscribeEvent
		public static void onKeyMappingsSetup(RegisterKeyMappingsEvent event)
		{
			logger.warn("CYBERWARE-INTERNAL: onKeyMappingsSetup");
			KeyBinds.register(event);
		}

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			logger.warn("CYBERWARE-INTERNAL: onClientSetup");
			MinecraftForge.EVENT_BUS.register(EssentialsMissingHandlerClient.INSTANCE);
			MinecraftForge.EVENT_BUS.register(CyberwareMenuHandler.INSTANCE);
			MinecraftForge.EVENT_BUS.register(HudHandler.INSTANCE);
			//ShaderUtil.init();

//			if (CyberwareConfig.INSTANCE.ENABLE_CLOTHES.get())
//			{
				Minecraft.getInstance().getItemColors().register(
					(stack, tintIndex) -> tintIndex > 0 ? -1 :
						((ItemArmorCyberware) stack.getItem()).getColor(stack),
					Armors.TRENCH_COAT.get()
				);
//			}
		}

		@SubscribeEvent
		public void onClientRegister(RegisterEvent event)
		{
			logger.warn("CYBERWARE-INTERNAL: ClientModEvents.onClientRegister");
			//			for (Block block : CyberwareContent.blocks)
			//			{
			//				registerRenders(block);
			//			}
			//
			//			for (Item item : CyberwareContent.items)
			//			{
			//				registerRenders(item);
			//			}

			//			ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySurgeryChamber.class, new
			//			TileEntitySurgeryChamberRenderer());
			//			RenderingRegistry.registerEntityRenderingHandler(EntityCyberZombie.class,
			//			RenderCyberZombie::new);
			//			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScanner.class, new
			//			TileEntityScannerRenderer());
			//			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEngineeringTable.class, new
			//			TileEntityEngineeringRenderer());
			//			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBeaconPost.TileEntityBeaconPostMaster
			//			.class, new TileEntityBeaconLargeRenderer());
		}

		//		@SubscribeEvent
		//		public void onWorldUnload(@Nonnull LevelEvent.Unload event) {
		//			if (!event.getLevel().isClientSide()
		//					&& !Minecraft.getInstance().getConnection().getConnection().isConnected()) {
		//				CyberwareConfig.loadConfig();
		//			}
		//		}

		private void registerRenders(Block block)
		{
			//			Item item = Item.byBlock(block);
			//			ModelLoader.setCustomModelResourceLocation(item,
			//					0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}

		private void registerRenders(Item item)
		{
			//			if (item instanceof ItemCyberware) {
			//				ItemCyberware ware = (ItemCyberware) item;
			//				List<ModelResourceLocation> models = new ArrayList<>();
			//				if (ware.subnames.length > 0) {
			//					for (int indexSubname = 0; indexSubname < ware.subnames.length; indexSubname++) {
			//						String name = ware.getRegistryName() + "_" + ware.subnames[indexSubname];
			//						for (ICyberware.Quality quality : ICyberware.Quality.qualities) {
			//							if (quality.getSpriteSuffix() != null
			//									&& ware.canHoldQuality(new ItemStack(ware, 1, indexSubname),
			//									quality)) {
			//								models.add(new ModelResourceLocation(name + "_" + quality.getSpriteSuffix
			//								(), "inventory"));
			//							}
			//						}
			//						models.add(new ModelResourceLocation(name, "inventory"));
			//					}
			//				} else {
			//					String name = ware.getRegistryName() + "";
			//
			//					for (ICyberware.Quality quality : ICyberware.Quality.qualities) {
			//						if (quality.getSpriteSuffix() != null
			//								&& ware.canHoldQuality(new ItemStack(ware), quality)) {
			//							models.add(new ModelResourceLocation(name + "_" + quality.getSpriteSuffix(),
			//							"inventory"));
			//						}
			//					}
			//					models.add(new ModelResourceLocation(name, "inventory"));
			//
			//				}
			//				ModelLoader.registerItemVariants(item, models.toArray(new ModelResourceLocation[0]));
			//				ModelLoader.setCustomMeshDefinition(item, new CyberwareMeshDefinition());
			//			} else if (item instanceof ItemBlueprint) {
			//				for (int i = 0; i < 2; i++) {
			//					ModelLoader.setCustomModelResourceLocation(item,
			//							i, new ModelResourceLocation(item.getRegistryName() + (i == 1 ? "_blank" : "")
			//							, "inventory"));
			//				}
			//			} else if (item instanceof ItemCyberwareBase) {
			//				ItemCyberwareBase base = ((ItemCyberwareBase) item);
			//				if (base.subnames.length > 0) {
			//					for (int indexSubname = 0; indexSubname < base.subnames.length; indexSubname++) {
			//						ModelLoader.setCustomModelResourceLocation(item,
			//								indexSubname, new ModelResourceLocation(item.getRegistryName() + "_" +
			//								base.subnames[indexSubname], "inventory"));
			//					}
			//				} else {
			//					ModelLoader.setCustomModelResourceLocation(item,
			//							0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			//				}
			//			} else {
			//				ModelLoader.setCustomModelResourceLocation(item,
			//						0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			//			}
		}
	}
}
