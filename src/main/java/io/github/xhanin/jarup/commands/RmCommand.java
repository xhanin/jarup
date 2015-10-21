package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;

public class RmCommand implements Command<RmCommand> {

    private WorkingCopy workingCopy;
    private String from;

    @Override
    public RmCommand baseOn(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    @Override
    public RmCommand parse(String[] args) {
        if (args.length != 1) {
            throw badArgument();
        }
        String arg = args[0];
        if (!arg.startsWith("--from=")) {
            throw badArgument();
        }

        this.from = (arg.substring("--from=".length()));
        return this;
    }

    @Override
    public void execute() throws IOException {
        workingCopy.deleteFile(from);
    }

    private IllegalArgumentException badArgument() {
        return new IllegalArgumentException("you must only provide file via `--from=` to rm");
    }
}
