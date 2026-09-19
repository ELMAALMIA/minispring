package io.minispring.container.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.Qualifier;
import io.minispring.container.exception.AmbiguousBeanException;
import io.minispring.container.exception.NoSuchBeanException;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;

class DependencyResolverTest {

    interface PaymentGateway {
    }

    static class StripeGateway implements PaymentGateway {
    }

    static class PaypalGateway implements PaymentGateway {
    }

    @Primary
    static class PreferredGateway implements PaymentGateway {
    }

    @Primary
    static class OtherPreferredGateway implements PaymentGateway {
    }

    static class Checkout {
        Checkout(@Qualifier("paypalGateway") PaymentGateway gateway) {
        }
    }

    static class Invoicing {
        Invoicing(PaymentGateway gateway) {
        }
    }

    @Test
    void resolvesTheOnlyBeanAssignableToAnInterface() {
        DependencyResolver resolver = resolverFor(StripeGateway.class);

        assertThat(resolver.resolve(Dependency.on(PaymentGateway.class)).type()).isEqualTo(StripeGateway.class);
    }

    @Test
    void qualifierSelectsTheNamedBeanAmongSeveral() {
        DependencyResolver resolver = resolverFor(StripeGateway.class, PaypalGateway.class);

        assertThat(resolver.resolve(parameterOf(Checkout.class, "checkout")).name()).isEqualTo("paypalGateway");
    }

    @Test
    void primaryBreaksATie() {
        DependencyResolver resolver = resolverFor(StripeGateway.class, PreferredGateway.class);

        assertThat(resolver.resolve(Dependency.on(PaymentGateway.class)).type()).isEqualTo(PreferredGateway.class);
    }

    @Test
    void throwsWhenTwoBeansMatchAndNeitherIsPrimary() {
        DependencyResolver resolver = resolverFor(StripeGateway.class, PaypalGateway.class);

        assertThatThrownBy(() -> resolver.resolve(parameterOf(Invoicing.class, "invoicing")))
                .isInstanceOf(AmbiguousBeanException.class)
                .hasMessageContaining("2 beans of type " + PaymentGateway.class.getName())
                .hasMessageContaining("parameter 'gateway' of bean 'invoicing'")
                .hasMessageContaining("[stripeGateway, paypalGateway]")
                .hasMessageContaining("@Primary");
    }

    @Test
    void throwsWhenSeveralCandidatesArePrimary() {
        DependencyResolver resolver = resolverFor(PreferredGateway.class, OtherPreferredGateway.class);

        assertThatThrownBy(() -> resolver.resolve(Dependency.on(PaymentGateway.class)))
                .isInstanceOf(AmbiguousBeanException.class)
                .hasMessageContaining("are marked @Primary")
                .hasMessageContaining("[preferredGateway, otherPreferredGateway]");
    }

    @Test
    void throwsWhenNothingMatchesAndNamesTheTypeAndTheRequester() {
        DependencyResolver resolver = resolverFor();

        assertThatThrownBy(() -> resolver.resolve(parameterOf(Invoicing.class, "invoicing")))
                .isInstanceOf(NoSuchBeanException.class)
                .hasMessageContaining("No bean of type " + PaymentGateway.class.getName())
                .hasMessageContaining("required by parameter 'gateway' of bean 'invoicing'");
    }

    @Test
    void throwsWhenTheQualifierNamesNoCandidate() {
        DependencyResolver resolver = resolverFor(StripeGateway.class);

        assertThatThrownBy(() -> resolver.resolve(parameterOf(Checkout.class, "checkout")))
                .isInstanceOf(NoSuchBeanException.class)
                .hasMessageContaining("No bean named 'paypalGateway'")
                .hasMessageContaining("Beans of that type: [stripeGateway]");
    }

    private static DependencyResolver resolverFor(Class<?>... types) {
        BeanRegistry registry = new BeanRegistry();
        BeanDefinitionReader reader = new BeanDefinitionReader();
        for (Class<?> type : types) {
            registry.register(reader.read(type));
        }
        return new DependencyResolver(registry);
    }

    private static Dependency parameterOf(Class<?> owner, String beanName) {
        Parameter parameter = owner.getDeclaredConstructors()[0].getParameters()[0];
        return Dependency.of(parameter, beanName);
    }
}
