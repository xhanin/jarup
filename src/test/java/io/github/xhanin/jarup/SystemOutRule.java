package io.github.xhanin.jarup;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Date: 10/1/14
 * Time: 18:19
 */
public class SystemOutRule implements TestRule {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    private void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    public String out() {
        return outContent.toString();
    }

    public String err() {
        return errContent.toString();
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUpStreams();
                try {
                    statement.evaluate();
                } finally {
                    cleanUpStreams();
                }
            }
        };
    }
}
