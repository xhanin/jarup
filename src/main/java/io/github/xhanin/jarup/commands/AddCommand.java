package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;

/**
 * Date: 10/1/14
 * Time: 18:22
 */
public class AddCommand implements Command<AddCommand> {
    private WorkingCopy workingCopy;
    private String from;
    private String to;

    public AddCommand() {
    }

    public AddCommand baseOn(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    @Override
    public AddCommand parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--from=")) {
                from(arg.substring("--from=".length()));
            } else if (arg.startsWith("--to=")) {
                to(arg.substring("--to=".length()));
            } else {
                throw new IllegalArgumentException("unknown parameter " + arg);
            }
        }

        if (from == null) {
            throw new IllegalArgumentException("you must provide from file to add");
        }
        if (to == null) {
            throw new IllegalArgumentException("you must provide to file to add");
        }

        return this;
    }

    public AddCommand from(String path) {
        this.from = path;
        return this;
    }

    public AddCommand to(String path) {
        this.to = path;
        return this;
    }

    @Override
    public void execute() throws IOException {
        if (from == null) {
            throw new IllegalArgumentException("you must provide from file to add");
        }
        if (to == null) {
            throw new IllegalArgumentException("you must provide to file to add");
        }

        workingCopy.copyFileFrom(from, to);
    }
}
