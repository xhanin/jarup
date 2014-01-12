package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.Command;
import io.github.xhanin.jarup.WorkingCopy;

import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Date: 10/1/14
 * Time: 22:46
 */
public class SetPropertyCommand implements Command<SetPropertyCommand> {
    private WorkingCopy workingCopy;
    private String path;
    private String from;
    private String charset;
    private Map<String, String> properties = new LinkedHashMap<>();

    @Override
    public SetPropertyCommand baseOn(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
        return this;
    }

    public SetPropertyCommand in(final String path) {
        this.path = path;
        return this;
    }

    public SetPropertyCommand withEncoding(final String charset) {
        this.charset = charset;
        return this;
    }

    public SetPropertyCommand set(String prop, String val) {
        properties.put(prop, val);
        return this;
    }


    public SetPropertyCommand from(String path) {
        this.from = path;
        return this;
    }

    @Override
    public SetPropertyCommand parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--encoding=")) {
                withEncoding(arg.substring("--encoding=".length()));
            } else if (arg.startsWith("--into=")) {
                in(arg.substring("--into=".length()));
            } else if (arg.startsWith("--from=")) {
                from(arg.substring("--from=".length()));
            } else {
                if (path == null) {
                    in(arg);
                } else {
                    int idx = arg.indexOf('=');
                    if (idx == -1) {
                        throw new IllegalArgumentException(
                                "invalid argument: it must have an equal sign to separate property from value. " + arg);
                    }
                    set(arg.substring(0, idx), arg.substring(idx + 1));
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
        if (charset == null) {
            charset = workingCopy.getDefaultCharsetFor(path);
        }

        if (from != null) {
            Properties p = new Properties();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(from), charset))) {
                p.load(reader);
            }
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }

        String s = workingCopy.readFile(path, charset);
        StringBuilder out = new StringBuilder();

        boolean updated = false;
        String[] lines = s.split("(?m)$");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.indexOf('=') != -1) {
                String p = line.substring(0, line.indexOf('='));
                if (properties.containsKey(p.trim())) {
                    updated = true;
                    out.append(p).append("=").append(properties.get(p.trim()));
                    properties.remove(p.trim());
                } else {
                    out.append(line);
                }
            } else {
                out.append(line);
            }
        }
        if (!properties.isEmpty()) {
            updated = true;
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                out.append("\n").append(entry.getKey()).append("=").append(entry.getValue());
            }
            out.append("\n");
        }

        if (updated) {
            workingCopy.writeFile(path, charset, out.toString());
        }
    }
}
