package flaxbeard.cyberware.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;

import javax.annotation.Nonnull;

public class CyberwareUpdateEvent extends EntityEvent
{
	private final LivingEntity entityLivingBase;
	private final ICyberwareUserData cyberwareUserData;

	public CyberwareUpdateEvent(@Nonnull LivingEntity entityLivingBase, @Nonnull ICyberwareUserData cyberwareUserData)
	{
		super(entityLivingBase);
		this.entityLivingBase = entityLivingBase;
		this.cyberwareUserData = cyberwareUserData;
	}

	@Nonnull
	public LivingEntity getEntity()
	{
		return entityLivingBase;
	}

	@Nonnull
	public ICyberwareUserData getCyberwareUserData()
	{
		return cyberwareUserData;
	}
}
