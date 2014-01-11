package io.github.xhanin.jarup;

import io.github.xhanin.jarup.commands.CatCommand;
import io.github.xhanin.jarup.commands.SearchReplaceCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Date: 10/1/14
 * Time: 18:38
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
        }

        if (args.length < 2) {
            usage();
            System.exit(1);
        }

        String jar = args[0];
        String command = args[1];

        Command c = getCommand(command);
        if (c == null) {
            usage();
            System.exit(1);
        }

        try (WorkingCopy workingCopy = WorkingCopy.prepareFor(Paths.get(jar))) {
            String[] commandArgs = new String[args.length - 2];
            System.arraycopy(args, 2, commandArgs, 0, commandArgs.length);
            c.baseOn(workingCopy).parse(commandArgs).execute();
        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Command getCommand(String command) {
        switch (command) {
            case "cat":
            case "extract":
                return new CatCommand();
            case "search-replace":
                return new SearchReplaceCommand();
            default:
                return null;
        }
    }

    private static void usage() {
        System.out.println("usage: jarup <jarfile> <command> <command-args>");
        System.out.println("check https://github.com/xhanin/jarup for details.");
    }
}
