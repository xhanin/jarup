package io.github.xhanin.jarup;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 17:27
 */
public class WorkingCopyTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_prepare_working_copy_and_read() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.jar");

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc).isNotNull();

            String s = wc.readFile("example.properties", "ISO-8859-1");

            assertThat(s).isEqualTo("property1=value\n" +
                    "prop=être ou ne pas être\n");
        }
    }

    @Test
    public void should_handle_jar_of_jar_for_read() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.war");

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc).isNotNull();

            String s = wc.readFile("WEB-INF/lib/example.jar:/example.properties", "ISO-8859-1");

            assertThat(s).isEqualTo("property1=value\n" +
                    "prop=être ou ne pas être\n");
        }
    }

    @Test
    public void should_update_jar_after_write() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.jar");

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc).isNotNull();

            wc.writeFile("test.txt", "UTF-8", "this is a test");
        }

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            String s = wc.readFile("test.txt", "UTF-8");
            assertThat(s).isEqualTo("this is a test");
        }
    }

    @Test
    public void should_update_jar_after_copy() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.jar");

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            wc.copyFileFrom("src/test/resources/example.properties", "example9.properties");
        }

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            String s = wc.readFile("example9.properties", "UTF-8");
            assertThat(s).isEqualTo("property1=newvalue\n" +
                    "property4=val4");
        }
    }

    @Test
    public void should_handle_jar_of_jar_for_write() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.war");

        long jarLengthBeforeUpdate;
        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            jarLengthBeforeUpdate = wc.getFile("WEB-INF/lib/example.jar").length();
            wc.writeFile("WEB-INF/lib/example.jar:/test.txt", "UTF-8", "this is a test");
        }

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc.getFile("WEB-INF/lib/example.jar").length()).isNotEqualTo(jarLengthBeforeUpdate);
            String s = wc.readFile("WEB-INF/lib/example.jar:/test.txt", "UTF-8");
            assertThat(s).isEqualTo("this is a test");
        }
    }

    @Test
    public void should_keep_jar_of_inside_untouched() throws Exception {
        Path exampleJarUnderTest = getJarUnderTest("example.war");

        long jarTimestamp = 1389540958000L;
        long jarLength = 1421L;
        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc.getFile("WEB-INF/lib/example.jar").lastModified()).isEqualTo(jarTimestamp);
            assertThat(wc.getFile("WEB-INF/lib/example.jar").length()).isEqualTo(jarLength);
            wc.writeFile("test.txt", "UTF-8", "test");
        }

        Thread.sleep(1500);

        try (WorkingCopy wc = WorkingCopy.prepareFor(exampleJarUnderTest)) {
            assertThat(wc.getFile("WEB-INF/lib/example.jar").lastModified()).isEqualTo(jarTimestamp);
            assertThat(wc.getFile("WEB-INF/lib/example.jar").length()).isEqualTo(jarLength);
            assertThat(wc.readFile("test.txt", "UTF-8")).isEqualTo("test");
        }
    }

    @Test
    public void should_update_jar_after_removal() throws Exception {
        Path archiveUnderTest = getJarUnderTest("example.jar");
        try (WorkingCopy wc = WorkingCopy.prepareFor(archiveUnderTest)) {
            assertThat(wc).isNotNull();
            wc.deleteFile("example.properties");
        }

        try (WorkingCopy wc = WorkingCopy.prepareFor(archiveUnderTest)) {
            assertThat(wc.getFile("example.properties")).doesNotExist();
            assertThat(wc.getFile("example2.properties")).exists();
        }
    }

    @Test
    public void should_handle_jar_of_jar_for_removal() throws Exception {
        Path archiveUnderTest = getJarUnderTest("example.war");
        try (WorkingCopy wc = WorkingCopy.prepareFor(archiveUnderTest)) {
            assertThat(wc).isNotNull();
            wc.deleteFile("WEB-INF/lib/example.jar:/example.properties");
        }

        thrown.expect(NoSuchFileException.class);
        try (WorkingCopy wc = WorkingCopy.prepareFor(archiveUnderTest)) {
            assertThat(wc.readFile("WEB-INF/lib/example.jar:/example.properties", "UTF-8")).isEmpty();
        }
    }



    private Path getJarUnderTest(String archive) throws IOException {
        File dir = tmp.newFolder();
        Path jarUnderTest = dir.toPath().resolve(archive);
        Files.copy(Paths.get("src/test/jars-content/" + archive), jarUnderTest);
        return jarUnderTest;
    }
}
