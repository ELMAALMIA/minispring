package io.minispring.container.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Component;
import io.minispring.container.exception.BeanDefinitionException;
import org.junit.jupiter.api.Test;

class BeanRegistryTest {

    private final BeanDefinitionReader reader = new BeanDefinitionReader();
    private final BeanRegistry registry = new BeanRegistry();

    static class PaymentService {
    }

    @Component("paymentService")
    static class LegacyPayments {
    }

    @Test
    void findsARegisteredDefinitionByName() {
        BeanDefinition definition = reader.read(PaymentService.class);

        registry.register(definition);

        assertThat(registry.contains("paymentService")).isTrue();
        assertThat(registry.find("paymentService")).contains(definition);
        assertThat(registry.find("unknown")).isEmpty();
    }

    @Test
    void rejectsTwoBeansWithTheSameNameAndNamesBothClasses() {
        registry.register(reader.read(PaymentService.class));
        BeanDefinition duplicate = reader.read(LegacyPayments.class);

        assertThatThrownBy(() -> registry.register(duplicate))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("Duplicate bean name 'paymentService'")
                .hasMessageContaining(PaymentService.class.getName())
                .hasMessageContaining(LegacyPayments.class.getName());
    }
}
