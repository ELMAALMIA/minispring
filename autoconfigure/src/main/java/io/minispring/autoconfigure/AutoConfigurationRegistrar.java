package io.minispring.autoconfigure;

import io.minispring.container.bean.BeanDefinition;
import io.minispring.container.condition.ConditionEvaluator.ConditionEvaluation;
import io.minispring.container.context.BeanDefinitionRegistrar;
import io.minispring.container.context.RegistrationContext;
import java.util.List;

/**
 * Registers the auto-configurations the classpath offers, and records why each one was kept or
 * skipped.
 *
 * <p>Add it with {@code AnnotationApplicationContext.builder().apply(new AutoConfigurationRegistrar())}.
 * It runs after the application's own beans, so every {@code @ConditionalOnMissingBean} inside an
 * auto-configuration sees what the application already declared.
 */
public final class AutoConfigurationRegistrar implements BeanDefinitionRegistrar {

    /** Set it to {@code true} to print the report, as {@code --debug} does in Spring Boot. */
    public static final String REPORT_PROPERTY = "minispring.autoconfigure.report";

    private final ConditionEvaluationReport report = new ConditionEvaluationReport();

    /** What was decided, once the context has been built. */
    public ConditionEvaluationReport report() {
        return report;
    }

    @Override
    public void registerDefinitions(RegistrationContext context) {
        List<Class<?>> autoConfigurations = AutoConfigurationSorter.sort(AutoConfigurationImports.load(context.classLoader()));
        autoConfigurations.forEach(autoConfiguration -> register(context, autoConfiguration));
        if (context.environment().getProperty(REPORT_PROPERTY).filter("true"::equalsIgnoreCase).isPresent()) {
            print(report.toText());
        }
    }

    private void register(RegistrationContext context, Class<?> autoConfiguration) {
        ConditionEvaluation evaluation = context.conditions().evaluate(autoConfiguration);
        report.record(autoConfiguration.getSimpleName(), evaluation.matches(), evaluation.reasons());
        if (!evaluation.matches()) {
            return;
        }
        context.registry().register(context.reader().read(autoConfiguration));
        for (BeanDefinition definition : context.reader().readBeanMethods(autoConfiguration)) {
            ConditionEvaluation beanEvaluation = context.conditions().evaluate(definition.annotatedElement());
            report.record(autoConfiguration.getSimpleName() + "#" + definition.name(), beanEvaluation.matches(), beanEvaluation.reasons());
            if (beanEvaluation.matches()) {
                context.registry().register(definition);
            }
        }
    }

    // S106: the report is meant to be read on the console, exactly like Spring Boot's.
    @SuppressWarnings("java:S106")
    private static void print(String text) {
        System.out.println(text);
    }
}
