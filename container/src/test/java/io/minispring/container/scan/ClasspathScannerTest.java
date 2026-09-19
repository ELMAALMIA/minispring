package io.minispring.container.scan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.fixtures.lazy.ExplodingInitializer;
import io.minispring.fixtures.scan.AbstractComponent;
import io.minispring.fixtures.scan.Alpha;
import io.minispring.fixtures.scan.Beta;
import io.minispring.fixtures.scan.ComponentInterface;
import io.minispring.fixtures.scan.Holder;
import io.minispring.fixtures.scan.NotAComponent;
import io.minispring.fixtures.scan.nested.Gamma;
import org.junit.jupiter.api.Test;

class ClasspathScannerTest {

    private final ClasspathScanner scanner = new ClasspathScanner();

    @Test
    void findsEveryComponentOfThePackageSortedByName() {
        assertThat(scanner.scan("io.minispring.fixtures.scan"))
                .containsExactly(Alpha.class, Beta.class, Holder.Nested.class, Gamma.class);
    }

    @Test
    void ignoresClassesWithoutTheAnnotation() {
        assertThat(scanner.scan("io.minispring.fixtures.scan")).doesNotContain(NotAComponent.class, Holder.class);
    }

    @Test
    void findsComponentsInNestedSubPackages() {
        assertThat(scanner.scan("io.minispring.fixtures.scan.nested")).containsExactly(Gamma.class);
    }

    @Test
    void ignoresClassesThatCannotBeInstantiated() {
        assertThat(scanner.scan("io.minispring.fixtures.scan"))
                .doesNotContain(AbstractComponent.class, ComponentInterface.class, Holder.Inner.class);
    }

    @Test
    void returnsNothingForAPackageThatDoesNotExist() {
        assertThat(scanner.scan("io.minispring.fixtures.missing")).isEmpty();
    }

    @Test
    void doesNotRunStaticInitializersOfScannedClasses() {
        assertThat(scanner.scan("io.minispring.fixtures.lazy"))
                .extracting(Class::getName)
                .containsExactly(ExplodingInitializer.class.getName());
    }

    @Test
    void rejectsABlankPackageInsteadOfScanningTheWholeClasspath() {
        assertThatThrownBy(() -> scanner.scan(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("whole classpath");
    }
}
