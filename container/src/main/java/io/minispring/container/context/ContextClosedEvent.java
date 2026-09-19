package io.minispring.container.context;

import io.minispring.container.event.ApplicationEvent;

/** Published when the context starts closing, while every singleton is still usable. */
public final class ContextClosedEvent extends ApplicationEvent {

    public ContextClosedEvent(ApplicationContext context) {
        super(context);
    }

    public ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
