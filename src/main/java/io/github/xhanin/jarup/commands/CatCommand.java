package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;

/**
 * Date: 10/1/14
 * Time: 18:22
 */
public class CatCommand implements Command<CatCommand> {
    private WorkingCopy workingCopy;
    private String path;
    private String charset;

    public CatCommand() {
    }

    public CatCommand in(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    @Override
    public CatCommand parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--encoding=")) {
                withEncoding(arg.substring("--encoding=".length()));
            } else {
                from(arg);
            }
        }

        if (path == null) {
            throw new IllegalArgumentException("you must provide file to cat");
        }

        return this;
    }

    @Override
    public void execute() throws IOException {
        if (path == null) {
            throw new IllegalStateException("path must be set");
        }
        if (charset == null) {
            if (path.endsWith(".properties")) {
                charset = "ISO-8859-1";
            } else {
                charset = "UTF-8";
            }
        }

        System.out.print(workingCopy.readFile(path, charset));
    }

    public CatCommand from(String path) {
        this.path = path;
        return this;
    }

    public CatCommand withEncoding(String encoding) {
        this.charset = encoding;
        return this;
    }
}
