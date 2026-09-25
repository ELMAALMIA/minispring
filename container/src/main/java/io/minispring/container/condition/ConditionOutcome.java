package io.minispring.container.condition;

import java.util.Objects;

/**
 * What a condition decided, and why.
 *
 * <p>The reason is not decoration: it is what lets the container answer the question every
 * developer eventually asks, "why is my bean not there?".
 *
 * @param match  whether the bean may be registered
 * @param reason a short sentence explaining the decision
 */
public record ConditionOutcome(boolean match, String reason) {

    public ConditionOutcome {
        Objects.requireNonNull(reason, "reason");
    }

    public static ConditionOutcome match(String reason) {
        return new ConditionOutcome(true, reason);
    }

    public static ConditionOutcome noMatch(String reason) {
        return new ConditionOutcome(false, reason);
    }
}
