package flaxbeard.cyberware.api.hud;

import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class CyberwareHudDataEvent extends Event
{
	private final List<IHudElement> elements = new ArrayList<>();

	public List<IHudElement> getElements()
	{
		return elements;
	}

	public void addElement(IHudElement element)
	{
		elements.add(element);
	}
}
