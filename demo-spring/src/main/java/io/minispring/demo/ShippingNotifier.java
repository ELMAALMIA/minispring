package io.minispring.demo;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;

/** Reacts to orders without the order service knowing it exists. */
@Component
public class ShippingNotifier implements ApplicationListener<OrderPlacedEvent> {

    @Override
    public void onApplicationEvent(OrderPlacedEvent event) {
        System.out.println("SHIP     " + event.getItem());
    }
}
