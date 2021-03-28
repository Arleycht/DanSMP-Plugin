package io.github.arleycht.SMP.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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

        switch (args[0].toLowerCase()) {
            case "eject":
                // Quick fix for some people who experience a server-client desync where passengers become stuck on
                // their vehicles (horses, specifically) even after relogging.

                Entity vehicle = player.getVehicle();

                if (vehicle != null) {
                    vehicle.eject();
                }

                return true;
            default:
                break;
        }

        return false;
    }
}
