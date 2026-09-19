package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.bean.BeanPostProcessor;
import io.minispring.container.exception.BeanCreationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostProcessorTest {

    interface Greeter {
        String greet();
    }

    static class EnglishGreeter implements Greeter {
        boolean initialized;

        @PostConstruct
        void init() {
            initialized = true;
        }

        @Override
        public String greet() {
            return "hello";
        }
    }

    static class Receptionist {
        final Greeter greeter;

        Receptionist(Greeter greeter) {
            this.greeter = greeter;
        }
    }

    static class RecordingPostProcessor implements BeanPostProcessor {
        final List<String> seen = new ArrayList<>();

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            boolean initialized = bean instanceof EnglishGreeter greeter && greeter.initialized;
            seen.add(beanName + (initialized ? " (initialized)" : ""));
            return bean;
        }
    }

    static class ShoutingPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof Greeter greeter) {
                return (Greeter) () -> greeter.greet().toUpperCase();
            }
            return bean;
        }
    }

    static class NullPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return null;
        }
    }

    @Test
    void seesEveryOtherBeanAfterItsPostConstructHasRun() {
        try (var context = AnnotationApplicationContext.of(Receptionist.class, EnglishGreeter.class, RecordingPostProcessor.class)) {
            assertThat(context.getBean(RecordingPostProcessor.class).seen)
                    .containsExactly("englishGreeter (initialized)", "receptionist");
        }
    }

    @Test
    void canReplaceABeanAndDependentsReceiveTheReplacement() {
        try (var context = AnnotationApplicationContext.of(Receptionist.class, EnglishGreeter.class, ShoutingPostProcessor.class)) {
            assertThat(context.getBean(Greeter.class).greet()).isEqualTo("HELLO");
            assertThat(context.getBean(Receptionist.class).greeter.greet()).isEqualTo("HELLO");
        }
    }

    @Test
    void failsClearlyWhenAPostProcessorReturnsNull() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(EnglishGreeter.class, NullPostProcessor.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Cannot create bean 'englishGreeter'")
                .hasMessageContaining(NullPostProcessor.class.getName() + " returned null");
    }
}
