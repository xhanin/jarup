package io.github.xhanin.jarup;

import java.io.IOException;

/**
 * Date: 10/1/14
 * Time: 18:43
 */
public interface Command<C extends Command> {
    public C in(WorkingCopy workingCopy);
    public C parse(String[] args);
    public void execute() throws IOException;
}
