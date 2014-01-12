package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.WorkingCopyRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 17:23
 */
public class AddCommandTest {
    @Rule
    public WorkingCopyRule wc = WorkingCopyRule.with("example.jar");

    @Test
    public void should_replace() throws Exception {
        new AddCommand().baseOn(wc.getWorkingCopy())
                .from("src/test/resources/example.xml").to("example1.xml")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>val</tag>\n" +
                        "</myxml>");
    }

    @Test
    public void should_add() throws Exception {
        new AddCommand().baseOn(wc.getWorkingCopy())
                .from("src/test/resources/example.xml").to("example-new.xml")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example-new.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>val</tag>\n" +
                        "</myxml>");
    }

    @Test
    public void should_add_in_new_directory() throws Exception {
        new AddCommand().baseOn(wc.getWorkingCopy())
                .from("src/test/resources/example.xml").to("new/example-new.xml")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("new/example-new.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>val</tag>\n" +
                        "</myxml>");
    }

    @Test
    public void should_parse_params() throws Exception {
        new AddCommand().baseOn(wc.getWorkingCopy())
                .parse(new String[] {"--from=src/test/resources/example.xml", "--to=example1.xml"})
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>val</tag>\n" +
                        "</myxml>");
    }
}
