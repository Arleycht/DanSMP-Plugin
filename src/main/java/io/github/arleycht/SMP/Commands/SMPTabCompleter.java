package io.github.arleycht.SMP.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SMPTabCompleter implements TabCompleter {
    public static final String[][] COMPLETIONS = {
            {"eject"}
    };

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();

        int argc = args.length;

        for (String[] expectedArgs : COMPLETIONS) {
            if (args.length <= expectedArgs.length) {
                // Check for partial completions on last inputted argument

                String expected = expectedArgs[argc - 1].toLowerCase();
                String partial = args[argc - 1].toLowerCase();

                if (expected.contains(partial)) {
                    completions.add(expected);
                }

                // TODO: Also check for special arguments (i.e. player names, UUIDS, numbers, etc.)
                // TODO: Create or use an existing formatter syntax to do the above
            }
        }

        return completions;
    }
}
