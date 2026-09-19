package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.exception.BeanCreationException;
import io.minispring.container.exception.BeanNotOfRequiredTypeException;
import io.minispring.container.exception.CircularDependencyException;
import io.minispring.container.exception.NoSuchBeanException;
import io.minispring.fixtures.scan.Alpha;
import io.minispring.fixtures.scan.nested.Gamma;
import org.junit.jupiter.api.Test;

class ConstructorInjectionTest {

    static class Repository {
    }

    static class Service {
        final Repository repository;

        Service(Repository repository) {
            this.repository = repository;
        }
    }

    static class Controller {
        final Service service;

        Controller(Service service) {
            this.service = service;
        }
    }

    interface Notifier {
    }

    static class EmailNotifier implements Notifier {
    }

    static class Alerts {
        final Notifier notifier;

        Alerts(Notifier notifier) {
            this.notifier = notifier;
        }
    }

    static class Chicken {
        Chicken(Egg egg) {
        }
    }

    static class Egg {
        Egg(Chicken chicken) {
        }
    }

    static class Rock {
        Rock(Paper paper) {
        }
    }

    static class Paper {
        Paper(Scissors scissors) {
        }
    }

    static class Scissors {
        Scissors(Rock rock) {
        }
    }

    static class Broken {
        Broken() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void emptyContextThrowsForAnUnknownType() {
        try (var context = AnnotationApplicationContext.of()) {
            assertThatThrownBy(() -> context.getBean(Repository.class))
                    .isInstanceOf(NoSuchBeanException.class)
                    .hasMessageContaining("No bean of type " + Repository.class.getName());
        }
    }

    @Test
    void injectsASingleDependency() {
        try (var context = AnnotationApplicationContext.of(Service.class, Repository.class)) {
            assertThat(context.getBean(Service.class).repository).isSameAs(context.getBean(Repository.class));
        }
    }

    @Test
    void injectsAChainThreeLevelsDeep() {
        try (var context = AnnotationApplicationContext.of(Controller.class, Service.class, Repository.class)) {
            Controller controller = context.getBean(Controller.class);

            assertThat(controller.service.repository).isSameAs(context.getBean(Repository.class));
        }
    }

    @Test
    void injectsByInterfaceWhenOneImplementationExists() {
        try (var context = AnnotationApplicationContext.of(Alerts.class, EmailNotifier.class)) {
            assertThat(context.getBean(Alerts.class).notifier).isInstanceOf(EmailNotifier.class);
        }
    }

    @Test
    void throwsOnATwoBeanCycle() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(Chicken.class, Egg.class))
                .isInstanceOf(CircularDependencyException.class)
                .hasMessageContaining("chicken -> egg -> chicken");
    }

    @Test
    void throwsOnAThreeBeanCycleAndShowsTheWholeChain() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(Rock.class, Paper.class, Scissors.class))
                .isInstanceOf(CircularDependencyException.class)
                .hasMessageContaining("rock -> paper -> scissors -> rock");
    }

    @Test
    void throwsWhenADependencyIsMissingAndNamesTheTypeAndTheRequester() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(Service.class))
                .isInstanceOf(NoSuchBeanException.class)
                .hasMessageContaining(Repository.class.getName())
                .hasMessageContaining("parameter 'repository' of bean 'service'");
    }

    @Test
    void reportsAFailingConstructorWithTheBeanNameAndTheOriginalCause() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(Broken.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Cannot create bean 'broken'")
                .hasMessageContaining("boom")
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void getsABeanByNameAndChecksItsType() {
        try (var context = AnnotationApplicationContext.of(Repository.class)) {
            assertThat(context.getBean("repository", Repository.class)).isNotNull();
            assertThatThrownBy(() -> context.getBean("repository", Service.class))
                    .isInstanceOf(BeanNotOfRequiredTypeException.class)
                    .hasMessageContaining("Bean 'repository' is a " + Repository.class.getName());
            assertThatThrownBy(() -> context.getBean("missing", Repository.class))
                    .isInstanceOf(NoSuchBeanException.class)
                    .hasMessageContaining("No bean named 'missing'");
        }
    }

    @Test
    void buildsTheContextFromAScannedPackage() {
        try (var context = AnnotationApplicationContext.scan("io.minispring.fixtures.scan")) {
            assertThat(context.containsBean("alpha")).isTrue();
            assertThat(context.containsBean("notAComponent")).isFalse();
            assertThat(context.getBean(Alpha.class)).isNotNull();
            assertThat(context.getBean(Gamma.class)).isNotNull();
        }
    }

    @Test
    void rejectsLookupsOnceClosed() {
        var context = AnnotationApplicationContext.of(Repository.class);
        context.close();

        assertThatThrownBy(() -> context.getBean(Repository.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }
}
