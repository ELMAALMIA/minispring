package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import io.minispring.container.exception.BeanCreationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LifecycleTest {

    static final List<String> EVENTS = new ArrayList<>();

    static class Repository {
        @PreDestroy
        void close() {
            EVENTS.add("destroy repository");
        }
    }

    static class Service {
        private final Repository repository;

        Service(Repository repository) {
            this.repository = repository;
        }

        @PostConstruct
        void init() {
            EVENTS.add("init service with repository " + (repository != null));
        }

        @PreDestroy
        void close() {
            EVENTS.add("destroy service");
        }
    }

    static class Controller {
        Controller(Service service) {
        }

        @PreDestroy
        void close() {
            EVENTS.add("destroy controller");
        }
    }

    @Scope(ScopeType.PROTOTYPE)
    static class Request {
        @PreDestroy
        void close() {
            EVENTS.add("destroy request");
        }
    }

    static class FailingShutdown {
        @PreDestroy
        void close() {
            throw new IllegalStateException("cannot close");
        }
    }

    static class FailingStartup {
        FailingStartup(Repository repository) {
        }

        @PostConstruct
        void init() {
            throw new IllegalStateException("cannot start");
        }
    }

    @BeforeEach
    void clearEvents() {
        EVENTS.clear();
    }

    @Test
    void postConstructRunsOnceAfterTheDependenciesAreInjected() {
        try (var context = AnnotationApplicationContext.of(Service.class, Repository.class)) {
            context.getBean(Service.class);

            assertThat(EVENTS).containsExactly("init service with repository true");
        }
    }

    @Test
    void closeRunsPreDestroyInReverseCreationOrder() {
        var context = AnnotationApplicationContext.of(Controller.class, Service.class, Repository.class);
        EVENTS.clear();

        context.close();

        assertThat(EVENTS).containsExactly("destroy controller", "destroy service", "destroy repository");
    }

    @Test
    void prototypesNeverReceivePreDestroy() {
        var context = AnnotationApplicationContext.of(Request.class);
        context.getBean(Request.class);

        context.close();

        assertThat(EVENTS).isEmpty();
    }

    @Test
    void closingTwiceDestroysEachBeanOnce() {
        var context = AnnotationApplicationContext.of(Repository.class);

        context.close();
        context.close();

        assertThat(EVENTS).containsExactly("destroy repository");
    }

    @Test
    void aFailingPreDestroyDoesNotStopTheOtherBeansFromClosing() {
        var context = AnnotationApplicationContext.of(Repository.class, FailingShutdown.class);

        context.close();

        assertThat(EVENTS).containsExactly("destroy repository");
    }

    @Test
    void aFailingPostConstructAbortsStartupAndDestroysTheBeansAlreadyCreated() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(FailingStartup.class, Repository.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Cannot create bean 'failingStartup'")
                .hasMessageContaining("@PostConstruct")
                .hasMessageContaining("cannot start");

        assertThat(EVENTS).containsExactly("destroy repository");
    }
}
