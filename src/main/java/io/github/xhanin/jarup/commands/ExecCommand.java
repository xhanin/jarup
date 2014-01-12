package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;

/**
 * Date: 12/1/14
 * Time: 18:25
 */
public class ExecCommand implements Command<ExecCommand> {
    private WorkingCopy workingCopy;

    private String[] command;

    public ExecCommand baseOn(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    public ExecCommand command(final String[] command) {
        this.command = command;
        return this;
    }

    @Override
    public ExecCommand parse(String[] args) {
        command(args);
        return this;
    }

    @Override
    public void execute() throws IOException {
        try {
            new ProcessBuilder(command).inheritIO().start().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
