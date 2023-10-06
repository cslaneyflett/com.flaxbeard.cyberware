package flaxbeard.cyberware.client.gui.hud;

import flaxbeard.cyberware.api.hud.IHudSaveData;
import net.minecraft.nbt.CompoundTag;

public class HudNBTData implements IHudSaveData
{
	private final CompoundTag tag;

	public HudNBTData(CompoundTag tag)
	{
		this.tag = tag;
	}

	@Override
	public void setString(String key, String s)
	{
		tag.putString(key, s);
	}

	@Override
	public String getString(String key)
	{
		return tag.getString(key);
	}

	@Override
	public void setBoolean(String key, boolean b)
	{
		tag.putBoolean(key, b);
	}

	@Override
	public boolean getBoolean(String key)
	{
		return tag.getBoolean(key);
	}

	@Override
	public void setFloat(String key, float f)
	{
		tag.putFloat(key, f);
	}

	@Override
	public float getFloat(String key)
	{
		return tag.getFloat(key);
	}

	@Override
	public void setInteger(String key, int i)
	{
		tag.putInt(key, i);
	}

	@Override
	public int getInteger(String key)
	{
		return tag.getInt(key);
	}

	public CompoundTag getTag()
	{
		return tag;
	}
}
