package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.hud.UpdateHudColorPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Optional;

public class CyberwarePacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(Cyberware.MODID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	static
	{
		CyberwareAPI.PACKET_HANDLER = INSTANCE;
	}

	public static void init()
	{
		INSTANCE.registerMessage(
			0,
			CyberwareSyncPacket.class,
			CyberwareSyncPacket::encode,
			CyberwareSyncPacket::decode,
			CyberwareSyncPacket.CyberwareSyncPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		INSTANCE.registerMessage(
			1,
			SurgeryRemovePacket.class,
			SurgeryRemovePacket::encode,
			SurgeryRemovePacket::decode,
			SurgeryRemovePacket.SurgeryRemovePacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			2,
			SwitchHeldItemAndRotationPacket.class,
			SwitchHeldItemAndRotationPacket::encode,
			SwitchHeldItemAndRotationPacket::decode,
			SwitchHeldItemAndRotationPacket.SwitchHeldItemAndRotationPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		INSTANCE.registerMessage(
			3,
			DodgePacket.class,
			DodgePacket::encode,
			DodgePacket::decode,
			DodgePacket.DodgePacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		//		INSTANCE.registerMessage(4 , GuiPacket.class, NetworkDirection.PLAY_TO_SERVER);
		INSTANCE.registerMessage(
			5,
			ParticlePacket.class,
			ParticlePacket::encode,
			ParticlePacket::decode,
			ParticlePacket.ParticlePacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		INSTANCE.registerMessage(
			6,
			EngineeringDestroyPacket.class,
			EngineeringDestroyPacket::encode,
			EngineeringDestroyPacket::decode,
			EngineeringDestroyPacket.EngineeringDestroyPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			7,
			ScannerSmashPacket.class,
			ScannerSmashPacket::encode,
			ScannerSmashPacket::decode,
			ScannerSmashPacket.ScannerSmashPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_CLIENT)
		);
		INSTANCE.registerMessage(
			8,
			EngineeringSwitchArchivePacket.class,
			EngineeringSwitchArchivePacket::encode,
			EngineeringSwitchArchivePacket::decode,
			EngineeringSwitchArchivePacket.EngineeringSwitchArchivePacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			9,
			SyncHotkeyPacket.class,
			SyncHotkeyPacket::encode,
			SyncHotkeyPacket::decode,
			SyncHotkeyPacket.SyncHotkeyPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			10,
			TriggerActiveAbilityPacket.class,
			TriggerActiveAbilityPacket::encode,
			TriggerActiveAbilityPacket::decode,
			TriggerActiveAbilityPacket.TriggerActiveAbilityPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			11,
			SyncHudDataPacket.class,
			SyncHudDataPacket::encode,
			SyncHudDataPacket::decode,
			SyncHudDataPacket.SyncHudDataPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			12,
			OpenRadialMenuPacket.class,
			OpenRadialMenuPacket::encode,
			OpenRadialMenuPacket::decode,
			OpenRadialMenuPacket.OpenRadialMenuPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
		INSTANCE.registerMessage(
			13,
			UpdateHudColorPacket.class,
			UpdateHudColorPacket::encode,
			UpdateHudColorPacket::decode,
			UpdateHudColorPacket.UpdateHudColorPacketHandler::handle,
			Optional.of(NetworkDirection.PLAY_TO_SERVER)
		);
	}
}
