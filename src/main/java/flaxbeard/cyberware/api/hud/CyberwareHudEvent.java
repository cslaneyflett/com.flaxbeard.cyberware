package flaxbeard.cyberware.api.hud;

import com.mojang.blaze3d.platform.Window;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class CyberwareHudEvent extends Event
{
	private final List<IHudElement> elements = new ArrayList<>();
	private boolean hudjackAvailable;
	private final Window window;

	public CyberwareHudEvent(Window window, boolean hudjackAvailable)
	{
		super();
		this.window = window;
		this.hudjackAvailable = hudjackAvailable;
	}

	public Window getWindow()
	{
		return window;
	}

	public boolean isHudjackAvailable()
	{
		return hudjackAvailable;
	}

	public void setHudjackAvailable(boolean hudjackAvailable)
	{
		this.hudjackAvailable = hudjackAvailable;
	}

	public List<IHudElement> getElements()
	{
		return elements;
	}

	public void addElement(IHudElement element)
	{
		elements.add(element);
	}
}
