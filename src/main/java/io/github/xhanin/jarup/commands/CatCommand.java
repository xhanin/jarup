package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;

/**
 * Date: 10/1/14
 * Time: 18:22
 */
public class CatCommand {
    private final WorkingCopy workingCopy;
    private String path;
    private String charset;

    public CatCommand(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
    }

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
