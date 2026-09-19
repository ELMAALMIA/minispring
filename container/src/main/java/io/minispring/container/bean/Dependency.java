package io.minispring.container.bean;

import io.minispring.container.annotation.Qualifier;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * A request for a bean: the type needed, an optional qualifier, and who is asking.
 *
 * @param type        the type the injection point needs
 * @param qualifier   the bean name required by {@link Qualifier}, or {@code null} if there is none
 * @param requestedBy the injection point, used in error messages, or {@code null} for a direct lookup
 */
public record Dependency(Class<?> type, String qualifier, String requestedBy) {

    public Dependency {
        Objects.requireNonNull(type, "type");
    }

    /** A direct lookup by type, as in {@code context.getBean(OrderService.class)}. */
    public static Dependency on(Class<?> type) {
        return new Dependency(type, null, null);
    }

    /** A constructor parameter of the bean {@code beanName}. */
    public static Dependency of(Parameter parameter, String beanName) {
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        return new Dependency(
                parameter.getType(),
                qualifier == null ? null : qualifier.value(),
                "parameter '%s' of bean '%s'".formatted(parameter.getName(), beanName));
    }

    /** The end of an error message sentence that says who needed the bean, if anyone did. */
    String requirement() {
        return requestedBy == null ? "" : ", required by " + requestedBy;
    }
}
