package flaxbeard.cyberware.common.misc;

// TODO
public class CommandClearCyberware // extends CommandBase
{
	//	@Nonnull
	//	@Override
	//	public String getName()
	//	{
	//		return "clearcyberware";
	//	}
	//
	//	@Nonnull
	//	@Override
	//	public String getUsage(@Nonnull ICommandSender sender)
	//	{
	//		return "cyberware.commands.clearCyberware.usage";
	//	}
	//
	//	@Override
	//	public int getRequiredPermissionLevel()
	//	{
	//		return 2;
	//	}
	//
	//	@Override
	//	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) throws CommandException
	//	{
	//		Player entityPlayer = args.length == 0 ? getCommandSenderAsPlayer(sender) : getPlayer(server, sender, args[0]);
	//
	//		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
	//		if (cyberwareUserData == null) return;
	//		cyberwareUserData.resetWare(entityPlayer);
	//		CyberwareAPI.updateData(entityPlayer);
	//
	//		notifyCommandListener(sender, this, "cyberware.commands.clearCyberware.success", entityPlayer.getName());
	//	}
	//
	//	@Nonnull
	//	@Override
	//	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
	//										  @Nullable BlockPos pos)
	//	{
	//		return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) :
	//			Collections.emptyList();
	//	}
	//
	//	@Override
	//	public boolean isUsernameIndex(String[] args, int index)
	//	{
	//		return index == 0;
	//	}
}