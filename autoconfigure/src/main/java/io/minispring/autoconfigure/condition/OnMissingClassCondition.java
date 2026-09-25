package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

/** Backs {@link ConditionalOnMissingClass}. */
public final class OnMissingClassCondition implements Condition {

    @Override
    public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
        ConditionalOnMissingClass unwanted = element.getAnnotation(ConditionalOnMissingClass.class);
        if (unwanted == null) {
            return ConditionOutcome.match("no class has to be absent");
        }
        return Arrays.stream(unwanted.name())
                .filter(name -> OnClassCondition.isPresent(name, context.classLoader()))
                .findFirst()
                .map(present -> ConditionOutcome.noMatch("class " + present + " is on the classpath"))
                .orElseGet(() -> ConditionOutcome.match("none of " + String.join(", ", unwanted.name()) + " is on the classpath"));
    }
}
