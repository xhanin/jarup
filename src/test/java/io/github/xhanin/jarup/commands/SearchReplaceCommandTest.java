package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.WorkingCopyRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 17:23
 */
public class SearchReplaceCommandTest {
    @Rule
    public WorkingCopyRule wc = WorkingCopyRule.with("example.jar");

    @Test
    public void should_search_replace() throws Exception {
        new SearchReplaceCommand().baseOn(wc.getWorkingCopy())
                .in("example1.xml").withEncoding("UTF-8")
                .replace("TOKEN").with("newvalue")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<myxml>\n" +
                                "    <tag>newvalue</tag>\n" +
                                "    <tag>{{test}}</tag>\n" +
                                "    <tag>être ou ne pas €tre</tag>\n" +
                                "</myxml>");
    }

    @Test
    public void should_search_replace_regex() throws Exception {
        new SearchReplaceCommand().baseOn(wc.getWorkingCopy())
                .in("example1.xml").withEncoding("UTF-8")
                .replaceRegex("\\{\\{(.+)\\}\\}").with("$1")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>TOKEN</tag>\n" +
                        "    <tag>test</tag>\n" +
                        "    <tag>être ou ne pas €tre</tag>\n" +
                        "</myxml>");
    }

    @Test
    public void should_parse_params() throws Exception {
        new SearchReplaceCommand().baseOn(wc.getWorkingCopy())
                .parse(new String[] {"example1.xml", "TOKEN", "newvalue"})
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>newvalue</tag>\n" +
                        "    <tag>{{test}}</tag>\n" +
                        "    <tag>être ou ne pas €tre</tag>\n" +
                        "</myxml>");

    }

    @Test
    public void should_parse_params_regex() throws Exception {
        new SearchReplaceCommand().baseOn(wc.getWorkingCopy())
                .parse(new String[] {"example1.xml", "/\\{\\{(.+)\\}\\}/", "$1"})
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example1.xml", "UTF-8"))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<myxml>\n" +
                        "    <tag>TOKEN</tag>\n" +
                        "    <tag>test</tag>\n" +
                        "    <tag>être ou ne pas €tre</tag>\n" +
                        "</myxml>");

    }
}
