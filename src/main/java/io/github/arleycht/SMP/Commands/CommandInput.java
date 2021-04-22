package io.github.arleycht.SMP.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandInput {
    private final String command;
    private final String baseCommand;
    private final ArrayList<String> args = new ArrayList<>();

    public CommandInput(String command) {
        this.command = command;

        List<String> tokens = Arrays.asList(command.split("\\s"));

        baseCommand = tokens.get(0);

        args.addAll(tokens.subList(1, tokens.size()));
    }

    public String getCommand() {
        return command;
    }

    public String getBaseCommand() {
        return baseCommand;
    }

    public List<String> getArgs() {
        return new ArrayList<>(args);
    }
}
