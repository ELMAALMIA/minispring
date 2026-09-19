package io.minispring.container.bean;

import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.Qualifier;
import io.minispring.container.exception.AmbiguousBeanException;
import io.minispring.container.exception.NoSuchBeanException;
import java.util.List;
import java.util.Objects;

/**
 * Chooses the single bean definition that satisfies a {@link Dependency}.
 *
 * <p>The candidates are the beans whose type can be assigned to the requested type. A
 * {@link Qualifier} narrows them down by name. Otherwise a single {@link Primary} candidate
 * wins. Anything still ambiguous is an error, because the container never guesses.
 */
public final class DependencyResolver {

    private final BeanRegistry registry;

    public DependencyResolver(BeanRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public BeanDefinition resolve(Dependency dependency) {
        List<BeanDefinition> candidates = registry.definitionsOfType(dependency.type());
        if (dependency.qualifier() != null) {
            return qualified(dependency, candidates);
        }
        return switch (candidates.size()) {
            case 0 -> throw new NoSuchBeanException("No bean of type %s is registered%s."
                    .formatted(dependency.type().getName(), dependency.requirement()));
            case 1 -> candidates.getFirst();
            default -> primary(dependency, candidates);
        };
    }

    private static BeanDefinition qualified(Dependency dependency, List<BeanDefinition> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate.name().equals(dependency.qualifier()))
                .findFirst()
                .orElseThrow(() -> new NoSuchBeanException("No bean named '%s' of type %s is registered%s. Beans of that type: %s."
                        .formatted(dependency.qualifier(), dependency.type().getName(), dependency.requirement(), names(candidates))));
    }

    private static BeanDefinition primary(Dependency dependency, List<BeanDefinition> candidates) {
        List<BeanDefinition> primaries = candidates.stream().filter(BeanDefinition::isPrimary).toList();
        if (primaries.size() == 1) {
            return primaries.getFirst();
        }
        if (primaries.isEmpty()) {
            throw new AmbiguousBeanException("%d beans of type %s match%s: %s. Mark one with @Primary, or choose one with @Qualifier(\"name\")."
                    .formatted(candidates.size(), dependency.type().getName(), dependency.requirement(), names(candidates)));
        }
        throw new AmbiguousBeanException("%d beans of type %s are marked @Primary%s: %s. At most one candidate can be @Primary."
                .formatted(primaries.size(), dependency.type().getName(), dependency.requirement(), names(primaries)));
    }

    private static List<String> names(List<BeanDefinition> definitions) {
        return definitions.stream().map(BeanDefinition::name).toList();
    }
}
