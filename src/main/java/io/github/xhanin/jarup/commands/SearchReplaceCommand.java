package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Date: 10/1/14
 * Time: 22:46
 */
public class SearchReplaceCommand implements Command<SearchReplaceCommand> {
    private WorkingCopy workingCopy;
    private String path;
    private String charset;
    private String searchStr;
    private String searchRegex;
    private String replace;

    @Override
    public SearchReplaceCommand baseOn(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    public SearchReplaceCommand in(final String path) {
        this.path = path;
        return this;
    }

    public SearchReplaceCommand withEncoding(final String charset) {
        this.charset = charset;
        return this;
    }

    public SearchReplaceCommand replace(final String searchStr) {
        this.searchStr = searchStr;
        return this;
    }

    public SearchReplaceCommand replaceRegex(final String searchRegex) {
        this.searchRegex = searchRegex;
        return this;
    }

    public SearchReplaceCommand with(final String replace) {
        this.replace = replace;
        return this;
    }

    @Override
    public SearchReplaceCommand parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--encoding=")) {
                withEncoding(arg.substring("--encoding=".length()));
            } else {
                if (path == null) {
                    path = arg;
                } else if (searchStr == null && searchRegex == null) {
                    if (arg.startsWith("/") && arg.endsWith("/")) {
                        searchRegex = arg.substring(1, arg.length() - 1);
                    } else {
                        searchStr = arg;
                    }
                } else if (replace == null) {
                    replace = arg;
                } else {
                    throw new IllegalArgumentException("too much argument provided");
                }
            }
        }
        return this;
    }

    @Override
    public void execute() throws IOException {
        if (path == null) {
            throw new IllegalStateException("path must be set");
        }
        if (searchStr == null && searchRegex == null) {
            throw new IllegalStateException("search must be set");
        }
        if (replace == null) {
            throw new IllegalStateException("replace must be set");
        }
        if (charset == null) {
            charset = workingCopy.getDefaultCharsetFor(path);
        }

        String s = workingCopy.readFile(path, charset);

        String updated;
        if (searchStr != null) {
            updated = s.replace(searchStr, replace);
        } else {
            updated = s.replaceAll(searchRegex, replace);
        }

        if (!updated.equals(s)) {
            workingCopy.writeFile(path, charset, updated);
        }
    }
}
