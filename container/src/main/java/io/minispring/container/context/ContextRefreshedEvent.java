package io.minispring.container.context;

import io.minispring.container.event.ApplicationEvent;

/** Published once every singleton has been created and the context is ready to use. */
public final class ContextRefreshedEvent extends ApplicationEvent {

    public ContextRefreshedEvent(ApplicationContext context) {
        super(context);
    }

    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
