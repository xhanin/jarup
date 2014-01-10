package io.github.xhanin.jarup;

import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 10/1/14
 * Time: 18:37
 */
public class JarupTest {
    @Rule
    public SystemOutRule sys = new SystemOutRule();

    @Test
    public void should_cat() throws Exception {
        Jarup.main(new String[]{"src/test/jars-content/example.jar", "cat", "example.properties"});

        assertThat(sys.err()).isEqualTo("");
        assertThat(sys.out()).isEqualTo("property1=value\n" +
                "prop=être ou ne pas être\n");
    }
}
