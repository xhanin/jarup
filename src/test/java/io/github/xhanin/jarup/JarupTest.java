package io.github.xhanin.jarup;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void should_load_commands_batch() throws Exception {
        List<Command> commands = Jarup.loadCommands(new String[]{"src/test/jars-content/example.jar",
                "cat", "example.properties",
                "+",
                "cat", "example/example.properties"
        });

        assertThat(commands).hasSize(2);
    }

    @Test
    public void should_load_commands_batch_from_file() throws Exception {
        List<Command> commands = Jarup.loadCommands(new String[]{"src/test/jars-content/example.jar",
                "batch", "src/test/resources/commands.jarup"
        });

        assertThat(commands).hasSize(2);
    }
}
