package nl.dcraft.ambientthoughts;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class SendMessageCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "ambientthoughtsmsg";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ambientthoughtsmsg <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP targetPlayer;

        try {
            if (args.length >= 1) {
                targetPlayer = getPlayer(sender, args[0]);
            } else {
                targetPlayer = getCommandSenderAsPlayer(sender);
            }
        } catch (PlayerNotFoundException e) {
            sender.addChatMessage(new ChatComponentText("Player not found."));
            return;
        }

        boolean sent = AmbientThoughts.proxy.sendManualMessageToPlayer(targetPlayer);

        if (sent) {
            sender.addChatMessage(
                new ChatComponentText("Ambient Thoughts message sent to " + targetPlayer.getCommandSenderName() + "."));
        } else {
            sender.addChatMessage(new ChatComponentText("Ambient Thoughts could not find a message to send."));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(
                args,
                MinecraftServer.getServer()
                    .getAllUsernames());
        }

        return null;
    }
}
