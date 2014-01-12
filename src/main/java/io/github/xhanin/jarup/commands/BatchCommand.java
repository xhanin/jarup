package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.CommandLoader;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Date: 11/1/14
 * Time: 14:54
 */
public class BatchCommand implements Command<BatchCommand> {
    private String path;

    @Override
    public BatchCommand baseOn(WorkingCopy workingCopy) {
        throw new UnsupportedOperationException("batch must load commands outside working copy");
    }

    @Override
    public BatchCommand parse(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("batch command must have command file as parameter");
        }
        from(args[0]);
        return this;
    }

    public BatchCommand from(String path) {
        this.path = path;
        return this;
    }

    public List<Command> load(CommandLoader commandLoader) throws IOException {
        List<Command> commands = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] args = line.split("\\s");
            commands.add(commandLoader.loadCommand(asList(args)));
        }
        return commands;
    }

    @Override
    public void execute() throws IOException {
        throw new UnsupportedOperationException("batch must load commands outside working copy");
    }
}
