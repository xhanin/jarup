package io.github.xhanin.jarup;

import io.github.xhanin.jarup.commands.AddCommand;
import io.github.xhanin.jarup.commands.BatchCommand;
import io.github.xhanin.jarup.commands.CatCommand;
import io.github.xhanin.jarup.commands.SearchReplaceCommand;

import java.util.List;

/**
 * Date: 11/1/14
 * Time: 15:00
 */
public class CommandLoader {
    public Command loadCommand(List<String> commandArgs) {
        if (commandArgs.isEmpty()) {
            throw new IllegalArgumentException("empty command");
        }
        Command command = getCommand(commandArgs.get(0));
        if (command == null) {
            throw new IllegalArgumentException("unknown command " + commandArgs.get(0));
        }
        return command.parse(commandArgs.subList(1, commandArgs.size()).toArray(new String[commandArgs.size() - 1]));
    }

    private Command getCommand(String command) {
        switch (command) {
            case "cat":
            case "extract":
                return new CatCommand();
            case "add":
            case "replace":
                return new AddCommand();
            case "search-replace":
                return new SearchReplaceCommand();
            case "batch":
                return new BatchCommand();
            default:
                return null;
        }
    }

}
