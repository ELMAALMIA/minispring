package io.minispring.demo;

import io.minispring.container.event.ApplicationEvent;

public class OrderPlacedEvent extends ApplicationEvent {

    private final String item;

    public OrderPlacedEvent(Object source, String item) {
        super(source);
        this.item = item;
    }

    public String getItem() {
        return item;
    }
}
