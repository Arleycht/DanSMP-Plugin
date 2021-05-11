package io.github.arleycht.SMP.Commands;

import io.github.arleycht.SMP.Artifacts.ArtifactManager;
import io.github.arleycht.SMP.Artifacts.IArtifact;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public class SMPCommandExecutor implements CommandExecutor {
    public static final String COMMAND_NAME = "dansmp";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
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
            case "artifacttest":
                ItemStack itemStack = player.getInventory().getItemInMainHand();

                Bukkit.broadcastMessage(MessageFormat.format("Tagging: {0} x{1}", itemStack.getType(), itemStack.getAmount()));

                if (itemStack.getAmount() > 0) {
                    final String artifactName = "The Test";

                    IArtifact artifact = ArtifactManager.getArtifact(artifactName);
                    Material artifactType = artifact.getType();

                    if (artifactType == null || itemStack.getType() == artifactType) {
                        ArtifactManager.tagItem(itemStack, artifactName);
                    }
                }

                return true;
            default:
                break;
        }

        return false;
    }
}
