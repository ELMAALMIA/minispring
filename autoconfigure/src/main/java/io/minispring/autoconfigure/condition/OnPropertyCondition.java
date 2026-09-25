package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

/** Backs {@link ConditionalOnProperty}. */
public final class OnPropertyCondition implements Condition {

    @Override
    public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
        ConditionalOnProperty required = element.getAnnotation(ConditionalOnProperty.class);
        if (required == null) {
            return ConditionOutcome.match("no property is required");
        }
        Optional<String> value = context.environment().getProperty(required.name());
        if (value.isEmpty()) {
            return required.matchIfMissing()
                    ? ConditionOutcome.match("property '%s' is not set, which is the default".formatted(required.name()))
                    : ConditionOutcome.noMatch("property '%s' is not set".formatted(required.name()));
        }
        if (value.get().equalsIgnoreCase(required.havingValue())) {
            return ConditionOutcome.match("property '%s' is '%s'".formatted(required.name(), value.get()));
        }
        return ConditionOutcome.noMatch("property '%s' is '%s', expected '%s'"
                .formatted(required.name(), value.get(), required.havingValue()));
    }
}
