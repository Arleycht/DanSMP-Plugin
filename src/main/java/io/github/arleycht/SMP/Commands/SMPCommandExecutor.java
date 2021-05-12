package io.github.arleycht.SMP.Commands;

import io.github.arleycht.SMP.Artifacts.ArtifactManager;
import io.github.arleycht.SMP.Artifacts.IArtifact;
import io.github.arleycht.SMP.util.Util;
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
            case "artifact":
                if (args.length > 1) {
                    switch (args[1].toLowerCase()) {
                        case "list":
                            player.sendMessage("Here are all the available artifacts:");

                            for (String name : ArtifactManager.getArtifactList()) {
                                player.sendMessage(name);
                            }

                            return true;
                        case "give":
                            StringBuilder builder = new StringBuilder();

                            for (int i = 2; i < args.length; ++i) {
                                if (i > 2) {
                                    builder.append(" ");
                                }

                                builder.append(args[i]);
                            }

                            String artifactName = builder.toString();
                            IArtifact artifact = ArtifactManager.getArtifact(artifactName);

                            if (artifact == null) {
                                player.sendMessage(MessageFormat.format("Artifact ''{0}'' doesn't exist!", artifactName));

                                return true;
                            }

                            Material artifactType = artifact.getType() != null ? artifact.getType() : Material.STICK;

                            ItemStack itemStack = new ItemStack(artifactType);

                            ArtifactManager.tagItem(itemStack, artifactName);

                            Util.giveItem(player, itemStack);

                            return true;
                        default:
                            break;
                    }
                }

                break;
            default:
                break;
        }

        return false;
    }
}
