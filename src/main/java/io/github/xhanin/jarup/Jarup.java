package io.github.xhanin.jarup;

import io.github.xhanin.jarup.commands.BatchCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Jarup main class.
 *
 * split args in commands, handle errors and usage.
 */
public class Jarup {
    public static void main(String[] args) {
        if (args.length == 1 && "gen-script".equals(args[0])) {
            if (!Paths.get("jarup.jar").toFile().exists()) {
                System.err.println("you must generate script in the directory where jarup.jar is located.");
                System.exit(1);
            }
            try {
                if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("windows")) {
                    Files.copy(Jarup.class.getResourceAsStream("/jarup.bat"), Paths.get("jarup.bat"));
                } else {
                    Files.copy(Jarup.class.getResourceAsStream("/jarup"), Paths.get("jarup"));
                    Paths.get("jarup").toFile().setExecutable(true);
                }
            } catch (IOException e) {
                System.err.println("error when generating script: " + e);
                System.exit(1);
            }
            return;
        }

        if (args.length < 2) {
            usage();
            System.exit(1);
        }

        String jar = args[0];

        List<Command> commands = new ArrayList<>();
        try {
            commands = loadCommands(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            usage();
            System.exit(1);
        }

        try (WorkingCopy workingCopy = WorkingCopy.prepareFor(Paths.get(jar))) {
            for (Command c : commands) {
                c.baseOn(workingCopy).execute();
            }
        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
            System.exit(1);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static List<Command> loadCommands(String[] args) throws IOException {
        CommandLoader commandLoader = new CommandLoader();
        List<Command> l = new ArrayList<>();
        List<String> commandArgs = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if ("+".equals(arg)) {
                loadCommands(commandLoader, l, commandArgs);
                commandArgs = new ArrayList<>();
            } else {
                commandArgs.add(arg);
            }
        }
        loadCommands(commandLoader, l, commandArgs);
        return l;
    }

    private static void loadCommands(CommandLoader commandLoader, List<Command> l, List<String> commandArgs) throws IOException {
        Command c = commandLoader.loadCommand(commandArgs);
        if (c instanceof BatchCommand) {
            l.addAll(((BatchCommand) c).load(commandLoader));
        } else {
            l.add(c);
        }
    }


    private static void usage() {
        System.out.println("usage: jarup <jarfile> <command> <command-args>");
        System.out.println("check https://github.com/xhanin/jarup for details.");
    }
}
