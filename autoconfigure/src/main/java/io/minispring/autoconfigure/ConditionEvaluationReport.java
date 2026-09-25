package io.minispring.autoconfigure;

import java.util.ArrayList;
import java.util.List;

/**
 * What every auto-configuration decision was, and why.
 *
 * <p>This is the answer to the question auto-configuration always raises: "why is that bean not
 * there?". Spring Boot prints the same kind of report when an application is started with
 * {@code --debug}.
 */
public final class ConditionEvaluationReport {

    /**
     * One decision, about an auto-configuration class or one of its {@code @Bean} methods.
     *
     * @param name       the class, or {@code Class#beanName} for a method
     * @param registered whether the bean was registered
     * @param reasons    what each condition said, in evaluation order
     */
    public record Decision(String name, boolean registered, List<String> reasons) {

        public Decision {
            reasons = List.copyOf(reasons);
        }
    }

    private final List<Decision> decisions = new ArrayList<>();

    void record(String name, boolean registered, List<String> reasons) {
        decisions.add(new Decision(name, registered, reasons));
    }

    public List<Decision> decisions() {
        return List.copyOf(decisions);
    }

    public List<Decision> positiveMatches() {
        return decisions.stream().filter(Decision::registered).toList();
    }

    public List<Decision> negativeMatches() {
        return decisions.stream().filter(decision -> !decision.registered()).toList();
    }

    /** The report as text, in the shape of Spring Boot's own. */
    public String toText() {
        StringBuilder text = new StringBuilder("CONDITION EVALUATION REPORT\n");
        append(text, "\nPositive matches:\n", positiveMatches());
        append(text, "\nNegative matches:\n", negativeMatches());
        return text.toString();
    }

    private static void append(StringBuilder text, String title, List<Decision> decisions) {
        text.append(title);
        if (decisions.isEmpty()) {
            text.append("  none\n");
            return;
        }
        for (Decision decision : decisions) {
            text.append("  ").append(decision.name()).append('\n');
            decision.reasons().forEach(reason -> text.append("    - ").append(reason).append('\n'));
        }
    }
}
