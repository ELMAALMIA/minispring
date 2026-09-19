package io.minispring.demo;

import io.minispring.container.annotation.Component;
import io.minispring.container.event.ApplicationListener;

/** Reacts to orders without the order service knowing it exists. */
@Component
public class ShippingNotifier implements ApplicationListener<OrderPlacedEvent> {

    @Override
    public void onApplicationEvent(OrderPlacedEvent event) {
        System.out.println("SHIP     " + event.getItem());
    }
}
