package io.minispring.demo;

import io.minispring.container.web.GetMapping;
import io.minispring.container.web.RestController;

@RestController
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/orders")
    public String listOrders() {
        return String.join(", ", orders.orders());
    }
}
