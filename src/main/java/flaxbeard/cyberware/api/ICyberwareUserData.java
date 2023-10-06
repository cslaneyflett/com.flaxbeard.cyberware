package flaxbeard.cyberware.api;

import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICyberwareUserData
{
	NonNullList<ItemStack> getInstalledCyberware(EnumSlot slot);

	void setInstalledCyberware(LivingEntity entityLivingBase, EnumSlot slot, List<ItemStack> cyberware);

	void setInstalledCyberware(LivingEntity entityLivingBase, EnumSlot slot, NonNullList<ItemStack> cyberware);

	boolean isCyberwareInstalled(ItemStack cyberware);

	int getCyberwareRank(ItemStack cyberware);

	CompoundTag serializeNBT();

	void deserializeNBT(CompoundTag tagCompound);

	boolean hasEssential(EnumSlot slot);

	void setHasEssential(EnumSlot slot, boolean hasLeft, boolean hasRight);

	ItemStack getCyberware(ItemStack cyberware);

	void updateCapacity();

	void resetBuffer();

	void addPower(int amount, ItemStack inputter);

	boolean isAtCapacity(ItemStack stack);

	boolean isAtCapacity(ItemStack stack, int buffer);

	float getPercentFull();

	int getCapacity();

	int getStoredPower();

	int getProduction();

	int getConsumption();

	boolean usePower(ItemStack stack, int amount);

	List<ItemStack> getPowerOutages();

	List<Integer> getPowerOutageTimes();

	void setImmune();

	boolean usePower(ItemStack stack, int amount, boolean isPassive);

	boolean hasEssential(EnumSlot slot, EnumSide side);

	void resetWare(LivingEntity entityLivingBase);

	int getNumActiveItems();

	List<ItemStack> getActiveItems();

	void removeHotkey(int i);

	void addHotkey(int i, ItemStack stack);

	ItemStack getHotkey(int i);

	Iterable<Integer> getHotkeys();

	List<ItemStack> getHudjackItems();

	void setHudData(CompoundTag tagCompound);

	CompoundTag getHudData();

	boolean hasOpenedRadialMenu();

	void setOpenedRadialMenu(boolean hasOpenedRadialMenu);

	void setHudColor(int color);

	void setHudColor(float[] color);

	int getHudColorHex();

	float[] getHudColor();

	int getMaxTolerance(@Nonnull LivingEntity entityLivingBase);

	void setTolerance(@Nonnull LivingEntity entityLivingBase, int amount);

	int getTolerance(@Nonnull LivingEntity entityLivingBase);

	@Deprecated
	int getEssence();

	@Deprecated
	void setEssence(int essence);

	@Deprecated
	int getMaxEssence();
}
