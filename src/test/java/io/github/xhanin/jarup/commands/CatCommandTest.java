package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.SystemOutRule;
import io.github.xhanin.jarup.WorkingCopyRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 18:12
 */
public class CatCommandTest {
    @Rule
    public WorkingCopyRule wc = WorkingCopyRule.with("example.jar");
    @Rule
    public SystemOutRule sys = new SystemOutRule();

    @Test
    public void should_cat_file() throws Exception {
        new CatCommand().baseOn(wc.getWorkingCopy())
                .from("example.properties").withEncoding("ISO-8859-1")
                .execute();

        assertThat(sys.out()).isEqualTo("property1=value\n" +
                "prop=être ou ne pas être\n");
    }

    @Test
    public void should_extract_file() throws Exception {
        File destFile = new File("target/example1.xml");
        if (destFile.exists()) {
            destFile.delete();
        }
        new CatCommand().baseOn(wc.getWorkingCopy())
                .from("example1.xml").to(destFile.toPath().toString())
                .execute();

        assertThat(destFile).exists().hasContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<myxml>\n" +
                "    <tag>TOKEN</tag>\n" +
                "    <tag>{{test}}</tag>\n" +
                "    <tag>être ou ne pas €tre</tag>\n" +
                "</myxml>");
    }

    @Test
    public void should_cat_properties_file_with_default_encoding() throws Exception {
        new CatCommand().baseOn(wc.getWorkingCopy())
                .from("example.properties")
                .execute();

        assertThat(sys.out()).isEqualTo("property1=value\n" +
                "prop=être ou ne pas être\n");
    }

    @Test
    public void should_cat_file_with_default_encoding() throws Exception {
        new CatCommand().baseOn(wc.getWorkingCopy())
                .from("example1.xml")
                .execute();

        assertThat(sys.out()).isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<myxml>\n" +
                "    <tag>TOKEN</tag>\n" +
                "    <tag>{{test}}</tag>\n" +
                "    <tag>être ou ne pas €tre</tag>\n" +
                "</myxml>");
    }
}
