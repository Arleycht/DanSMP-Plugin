package io.github.arleycht.SMP.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMPCommandExecutor implements CommandExecutor {
    public static final String COMMAND_NAME = "dansmp";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;

        if (!label.equalsIgnoreCase(COMMAND_NAME)) {
            return false;
        }

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        for (String arg : args) {
            System.out.println(arg);
        }

        switch (args[0].toLowerCase()) {
            case "funny":
                String msg = "%s used the funny command!\nPlease laugh.";

                Bukkit.broadcastMessage(String.format(msg, player.getName()));
                player.chat(":^)");
                player.setHealth(0.0);

                return true;
            default:
                break;
        }

        return false;
    }
}
