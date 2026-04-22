package nl.dcraft.ambientthoughts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ReloadMessagesCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "ambientthoughtsreload";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ambientthoughtsreload";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Config.syncConfig();
        AmbientThoughts.proxy.reloadMessages();

        sender.addChatMessage(new ChatComponentText("Ambient Thoughts config and messages reloaded."));
        AmbientThoughts.LOG.info("Ambient Thoughts reloaded by " + sender.getCommandSenderName());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
