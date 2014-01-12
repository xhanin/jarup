package io.github.xhanin.jarup.commands;

import io.github.xhanin.jarup.WorkingCopyRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 17:23
 */
public class SetPropertyCommandTest {
    @Rule
    public WorkingCopyRule wc = WorkingCopyRule.with("example.jar");

    @Test
    public void should_set_property_default_encoding() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .in("example.properties")
                .set("property1", "newvalue")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example.properties", "ISO-8859-1"))
                .isEqualTo("property1=newvalue\n" +
                        "prop=être ou ne pas être\n");
    }

    @Test
    public void should_set_all_properties_default_encoding() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .in("example.properties")
                .set("property1", "newvalue")
                .set("prop", "newvalue2")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example.properties", "ISO-8859-1"))
                .isEqualTo("property1=newvalue\n" +
                        "prop=newvalue2\n");
    }

    @Test
    public void should_set_new_property() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .in("example2.properties").withEncoding("UTF-8")
                .set("property5", "newvalue")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example2.properties", "UTF-8"))
                .isEqualTo("# this is encoded in UTF-8\n" +
                        "\n" +
                        "property1=value €\n" +
                        "prop=être ou ne pas €tre\n" +
                        "property5=newvalue\n");
    }

    @Test
    public void should_set_properties_from_file() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .in("example.properties")
                .from("src/test/resources/example.properties")
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example.properties", "ISO-8859-1"))
                .isEqualTo("property1=newvalue\n" +
                        "prop=être ou ne pas être\n" +
                        "\n" +
                        "property4=val4\n");
    }

    @Test
    public void should_parse_params() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .parse(new String[] {"example.properties", "property1=newvalue"})
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example.properties", "ISO-8859-1"))
                .isEqualTo("property1=newvalue\n" +
                        "prop=être ou ne pas être\n");

    }

    @Test
    public void should_parse_params_load_from_file() throws Exception {
        new SetPropertyCommand().baseOn(wc.getWorkingCopy())
                .parse(new String[] {"--from=src/test/resources/example.properties", "--into=example.properties"})
                .execute();

        assertThat(wc.getWorkingCopy().readFile("example.properties", "ISO-8859-1"))
                .isEqualTo("property1=newvalue\n" +
                        "prop=être ou ne pas être\n" +
                        "\n" +
                        "property4=val4\n");

    }
}
