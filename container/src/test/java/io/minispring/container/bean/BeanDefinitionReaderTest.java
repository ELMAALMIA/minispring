package io.minispring.container.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Autowired;
import io.minispring.container.annotation.Component;
import io.minispring.container.exception.BeanDefinitionException;
import org.junit.jupiter.api.Test;

class BeanDefinitionReaderTest {

    private final BeanDefinitionReader reader = new BeanDefinitionReader();

    static class OrderRepository {
    }

    static class OrderService {
        OrderService(OrderRepository repository) {
        }
    }

    @Component("custom")
    static class NamedService {
    }

    static class URLParser {
    }

    static class TwoConstructors {
        TwoConstructors() {
        }

        @Autowired
        TwoConstructors(OrderRepository repository) {
        }
    }

    static class AmbiguousConstructors {
        AmbiguousConstructors() {
        }

        AmbiguousConstructors(OrderRepository repository) {
        }
    }

    static class TwoAutowiredConstructors {
        @Autowired
        TwoAutowiredConstructors() {
        }

        @Autowired
        TwoAutowiredConstructors(OrderRepository repository) {
        }
    }

    @Test
    void derivesTheBeanNameFromTheClassName() {
        assertThat(reader.read(OrderService.class).name()).isEqualTo("orderService");
    }

    @Test
    void usesTheExplicitComponentNameWhenGiven() {
        assertThat(reader.read(NamedService.class).name()).isEqualTo("custom");
    }

    @Test
    void keepsANameThatStartsWithAnAcronymUnchanged() {
        assertThat(reader.read(URLParser.class).name()).isEqualTo("URLParser");
    }

    @Test
    void selectsTheOnlyConstructorWithoutNeedingAutowired() throws NoSuchMethodException {
        assertThat(reader.read(OrderService.class).constructor())
                .isEqualTo(OrderService.class.getDeclaredConstructor(OrderRepository.class));
    }

    @Test
    void selectsTheAutowiredConstructorAmongSeveral() throws NoSuchMethodException {
        assertThat(reader.read(TwoConstructors.class).constructor())
                .isEqualTo(TwoConstructors.class.getDeclaredConstructor(OrderRepository.class));
    }

    @Test
    void throwsWhenSeveralConstructorsExistAndNoneIsAutowired() {
        assertThatThrownBy(() -> reader.read(AmbiguousConstructors.class))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("none is annotated with @Autowired")
                .hasMessageContaining("AmbiguousConstructors()")
                .hasMessageContaining("AmbiguousConstructors(OrderRepository)");
    }

    @Test
    void throwsWhenSeveralConstructorsAreAutowired() {
        assertThatThrownBy(() -> reader.read(TwoAutowiredConstructors.class))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("2 are annotated with @Autowired");
    }
}
