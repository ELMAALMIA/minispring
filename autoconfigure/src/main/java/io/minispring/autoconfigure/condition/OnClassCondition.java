package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

/** Backs {@link ConditionalOnClass} and {@link ConditionalOnMissingClass}. */
public final class OnClassCondition implements Condition {

    @Override
    public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
        ConditionalOnClass required = element.getAnnotation(ConditionalOnClass.class);
        if (required == null) {
            return ConditionOutcome.match("no class is required");
        }
        return Arrays.stream(required.name())
                .filter(name -> !isPresent(name, context.classLoader()))
                .findFirst()
                .map(missing -> ConditionOutcome.noMatch("required class " + missing + " is not on the classpath"))
                .orElseGet(() -> ConditionOutcome.match("found required class(es) " + String.join(", ", required.name())));
    }

    /** Loads the class without initializing it: asking whether a class exists must have no side effect. */
    static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
