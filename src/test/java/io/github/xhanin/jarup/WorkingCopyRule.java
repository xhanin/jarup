package io.github.xhanin.jarup;

import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Date: 10/1/14
 * Time: 18:12
 */
public class WorkingCopyRule implements TestRule {
    public static WorkingCopyRule with(String jar) {
        return new WorkingCopyRule(jar);
    }

    private static Path getJarUnderTest(TemporaryFolder tmp, String jar) throws IOException {
        File dir = tmp.newFolder();
        Path jarUnderTest = dir.toPath().resolve(jar);
        Files.copy(Paths.get("src/test/jars-content/" + jar), jarUnderTest);
        return jarUnderTest;
    }


    private final String jar;
    private WorkingCopy wc;

    public WorkingCopyRule(String jar) {
        this.jar = jar;
    }

    public WorkingCopy getWorkingCopy() {
        return wc;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        final TemporaryFolder tmp = new TemporaryFolder();
        return tmp.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    wc = WorkingCopy.prepareFor(getJarUnderTest(tmp, jar));
                    statement.evaluate();
                } finally {
                    if (wc != null) {
                        wc.close();
                    }
                }
            }
        }, description);
    }
}
