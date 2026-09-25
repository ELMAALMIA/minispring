package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

/** Backs {@link ConditionalOnBean}. */
public final class OnBeanCondition implements Condition {

    @Override
    public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
        ConditionalOnBean annotation = element.getAnnotation(ConditionalOnBean.class);
        if (annotation == null) {
            return ConditionOutcome.match("no bean is required");
        }
        List<Class<?>> types = SearchedTypes.of(annotation.value(), element);
        for (Class<?> type : types) {
            if (context.registry().definitionsOfType(type).isEmpty()) {
                return ConditionOutcome.noMatch("no bean of type " + type.getSimpleName() + " is registered");
            }
        }
        return ConditionOutcome.match("found a bean of type " + SearchedTypes.describe(types));
    }
}
