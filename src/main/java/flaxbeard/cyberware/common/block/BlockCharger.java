package flaxbeard.cyberware.common.block;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityCharger;
import net.minecraft.item.ItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class BlockCharger extends Block
{
	public BlockCharger()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);

		String name = "charger";

		setRegistryName(name);
		// ForgeRegistries.BLOCKS.register(this);

		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.charger.0", "cyberware.tooltip.charger" +
			".1");
		itemBlock.setRegistryName(name);
		// ForgeRegistries.ITEMS.register(itemBlock);

		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityCharger.class, new ResourceLocation(Cyberware.MODID, name));

		CyberwareContent.blocks.add(this);
	}

	@Override
	public BlockEntity createNewTileEntity(@Nonnull Level world, int metadata)
	{
		return new TileEntityCharger();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(BlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}
}
