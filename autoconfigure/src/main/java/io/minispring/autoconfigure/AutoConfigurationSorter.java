package io.minispring.autoconfigure;

import static java.util.stream.Collectors.joining;

import io.minispring.container.exception.BeanDefinitionException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Puts auto-configurations in the order they are registered, which decides who gets to answer
 * "is that bean already there?" first.
 *
 * <p>The list is first ordered by {@link AutoConfigureOrder} and then by name, so the result is
 * stable. {@link AutoConfigureBefore} and {@link AutoConfigureAfter} are then applied as
 * constraints, keeping the initial order whenever the constraints allow it.
 */
final class AutoConfigurationSorter {

    private AutoConfigurationSorter() {
    }

    static List<Class<?>> sort(List<Class<?>> autoConfigurations) {
        List<Class<?>> candidates = new ArrayList<>(autoConfigurations);
        candidates.sort(Comparator.comparingInt(AutoConfigurationSorter::orderOf).thenComparing(Class::getName));
        Map<Class<?>, Set<Class<?>>> predecessors = predecessorsOf(candidates);

        List<Class<?>> sorted = new ArrayList<>();
        List<Class<?>> remaining = new ArrayList<>(candidates);
        while (!remaining.isEmpty()) {
            Optional<Class<?>> ready = remaining.stream()
                    .filter(candidate -> sorted.containsAll(predecessors.get(candidate)))
                    .findFirst();
            Class<?> next = ready.orElseThrow(() -> new BeanDefinitionException(
                    "Auto-configurations cannot be ordered, @AutoConfigureBefore and @AutoConfigureAfter form a cycle between: "
                            + remaining.stream().map(Class::getSimpleName).collect(joining(", "))));
            sorted.add(next);
            remaining.remove(next);
        }
        return List.copyOf(sorted);
    }

    /** For each auto-configuration, those that must be registered before it. */
    private static Map<Class<?>, Set<Class<?>>> predecessorsOf(List<Class<?>> candidates) {
        Map<Class<?>, Set<Class<?>>> predecessors = new HashMap<>();
        candidates.forEach(candidate -> predecessors.put(candidate, new HashSet<>()));
        for (Class<?> candidate : candidates) {
            AutoConfigureAfter after = candidate.getAnnotation(AutoConfigureAfter.class);
            if (after != null) {
                addAll(predecessors.get(candidate), after.value(), candidates);
            }
            AutoConfigureBefore before = candidate.getAnnotation(AutoConfigureBefore.class);
            if (before != null) {
                // "X before Y" is the same constraint as "Y after X".
                for (Class<?> successor : before.value()) {
                    if (candidates.contains(successor)) {
                        predecessors.get(successor).add(candidate);
                    }
                }
            }
        }
        return predecessors;
    }

    /** Classes that are not on the classpath are simply ignored, as they cannot be registered anyway. */
    private static void addAll(Set<Class<?>> target, Class<?>[] declared, List<Class<?>> candidates) {
        for (Class<?> type : declared) {
            if (candidates.contains(type)) {
                target.add(type);
            }
        }
    }

    private static int orderOf(Class<?> autoConfiguration) {
        AutoConfigureOrder order = autoConfiguration.getAnnotation(AutoConfigureOrder.class);
        return order == null ? 0 : order.value();
    }
}
