package io.minispring.container.condition;

import io.minispring.container.exception.BeanDefinitionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runs the conditions of one class or {@code @Bean} method and reports what they decided.
 *
 * <p>Conditions declared through {@link Conditional} are collected from the element itself and
 * from its annotations, so an annotation such as {@code @ConditionalOnProperty} is simply a
 * {@code @Conditional} with a name. Evaluation stops at the first mismatch, like Spring's.
 */
public final class ConditionEvaluator {

    private final ConditionContext context;
    private final Map<Class<? extends Condition>, Condition> conditions = new HashMap<>();

    public ConditionEvaluator(ConditionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public ConditionEvaluation evaluate(AnnotatedElement element) {
        List<ConditionOutcome> outcomes = new ArrayList<>();
        for (Class<? extends Condition> type : conditionTypesOf(element)) {
            ConditionOutcome outcome = instantiate(type).matches(context, element);
            outcomes.add(outcome);
            if (!outcome.match()) {
                break;
            }
        }
        return new ConditionEvaluation(List.copyOf(outcomes));
    }

    /** Conditions written directly on the element, plus those carried by its annotations. */
    private static List<Class<? extends Condition>> conditionTypesOf(AnnotatedElement element) {
        List<Class<? extends Condition>> types = new ArrayList<>();
        Conditional direct = element.getAnnotation(Conditional.class);
        if (direct != null) {
            types.addAll(List.of(direct.value()));
        }
        for (Annotation annotation : element.getAnnotations()) {
            Conditional meta = annotation.annotationType().getAnnotation(Conditional.class);
            if (meta != null) {
                types.addAll(List.of(meta.value()));
            }
        }
        return types;
    }

    private Condition instantiate(Class<? extends Condition> type) {
        return conditions.computeIfAbsent(type, ConditionEvaluator::newCondition);
    }

    private static Condition newCondition(Class<? extends Condition> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new BeanDefinitionException("Condition %s needs a constructor without arguments".formatted(type.getName()), e);
        }
    }

    /** Everything the conditions of one element decided. */
    public record ConditionEvaluation(List<ConditionOutcome> outcomes) {

        public ConditionEvaluation {
            outcomes = List.copyOf(outcomes);
        }

        public boolean matches() {
            return outcomes.stream().allMatch(ConditionOutcome::match);
        }

        /** The reasons, in evaluation order; the last one is why a rejected bean was rejected. */
        public List<String> reasons() {
            return outcomes.stream().map(ConditionOutcome::reason).toList();
        }

        public String summary() {
            return String.join("; ", reasons());
        }
    }
}
