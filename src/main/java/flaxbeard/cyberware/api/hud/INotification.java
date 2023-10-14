package flaxbeard.cyberware.api.hud;

import com.mojang.blaze3d.vertex.PoseStack;

public interface INotification
{
	public void render(PoseStack poseStack, int x, int y);

	public int getDuration();
}
