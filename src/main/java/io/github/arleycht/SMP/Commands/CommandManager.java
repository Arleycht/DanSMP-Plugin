package io.github.arleycht.SMP.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class CommandManager implements CommandExecutor, TabCompleter {
    private static HashMap<String, Consumer<CommandInput>> COMMANDS;

    public static void registerCommand(String commandLine, Consumer<CommandInput> consumer) {
        COMMANDS.put(commandLine, consumer);
    }

    public static List<String> getCommands() {
        return new ArrayList<>(COMMANDS.keySet());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
