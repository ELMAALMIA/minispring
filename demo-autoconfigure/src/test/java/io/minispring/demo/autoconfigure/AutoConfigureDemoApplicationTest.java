package io.minispring.demo.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AutoConfigureDemoApplicationTest {

    private static final Path EXPECTED_OUTPUT = Path.of("..", "docs", "autoconfigure-demo-output.txt");

    @Test
    void printsWhatEachRunDecided() throws Exception {
        PrintStream original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            AutoConfigureDemoApplication.main(new String[0]);
        } finally {
            System.setOut(original);
        }

        List<String> expected = Files.readAllLines(EXPECTED_OUTPUT, StandardCharsets.UTF_8);
        assertThat(buffer.toString(StandardCharsets.UTF_8).lines()).containsExactlyElementsOf(expected);
    }
}
