package io.minispring.autoconfigure.condition;

import io.minispring.container.bean.BeanDefinition;
import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

/** Backs {@link ConditionalOnMissingBean}. */
public final class OnMissingBeanCondition implements Condition {

    @Override
    public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
        ConditionalOnMissingBean annotation = element.getAnnotation(ConditionalOnMissingBean.class);
        if (annotation == null) {
            return ConditionOutcome.match("no bean has to be missing");
        }
        List<Class<?>> types = SearchedTypes.of(annotation.value(), element);
        for (Class<?> type : types) {
            List<BeanDefinition> existing = context.registry().definitionsOfType(type);
            if (!existing.isEmpty()) {
                return ConditionOutcome.noMatch("bean %s of type %s is already registered"
                        .formatted(existing.getFirst().name(), type.getSimpleName()));
            }
        }
        return ConditionOutcome.match("no bean of type " + SearchedTypes.describe(types) + " is registered");
    }
}
