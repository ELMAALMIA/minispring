package io.minispring.container.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConsoleTransactionManagerTest {

    @Test
    void printsEachTransactionBoundaryOnItsOwnAlignedLine() {
        var buffer = new ByteArrayOutputStream();
        var manager = new ConsoleTransactionManager(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        manager.begin("OrderService.placeOrder");
        manager.commit("OrderService.placeOrder");
        manager.rollback("OrderService.placeOrder");

        assertThat(buffer.toString(StandardCharsets.UTF_8).lines()).containsExactly(
                "BEGIN    OrderService.placeOrder",
                "COMMIT   OrderService.placeOrder",
                "ROLLBACK OrderService.placeOrder");
    }
}
