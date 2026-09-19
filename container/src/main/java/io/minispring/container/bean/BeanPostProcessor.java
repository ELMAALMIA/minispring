package io.minispring.container.bean;

/**
 * Extension point that sees every bean once it is fully initialized.
 *
 * <p>Post-processors are beans themselves. The container creates them before any other bean
 * and passes every later bean through them. A post-processor may return a <em>different
 * object</em> than the one it received, for example a proxy. That is how {@code @Transactional}
 * works.
 */
public interface BeanPostProcessor {

    /**
     * Called after injection and {@code @PostConstruct}. Returns the object the container should
     * hand out: the bean itself or a replacement, never {@code null}.
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
