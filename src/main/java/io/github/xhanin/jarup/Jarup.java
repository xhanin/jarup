package io.github.xhanin.jarup;

import io.github.xhanin.jarup.commands.CatCommand;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Date: 10/1/14
 * Time: 18:38
 */
public class Jarup {
    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            return;
        }

        String jar = args[0];
        String command = args[1];

        Command c = getCommand(command);
        if (c == null) {
            usage();
            return;
        }

        try (WorkingCopy workingCopy = WorkingCopy.prepareFor(Paths.get(jar))) {
            String[] commandArgs = new String[args.length - 2];
            System.arraycopy(args, 2, commandArgs, 0, commandArgs.length);
            c.in(workingCopy).parse(commandArgs).execute();
        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private static Command getCommand(String command) {
        if ("cat".equals(command)) {
            return new CatCommand();
        }
        return null;
    }

    private static void usage() {
        System.out.println("usage: jarup <jarfile> <command> <command-args>");
        System.out.println("check https://github.com/xhanin/jarup for details.");
    }
}
