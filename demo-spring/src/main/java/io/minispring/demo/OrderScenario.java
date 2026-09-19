package io.minispring.demo;

import java.util.List;

/** The scenario both demos run. It uses no framework type, so it is identical in both modules. */
public final class OrderScenario {

    private OrderScenario() {
    }

    public static void run(OrderService orders, OrderController controller) {
        System.out.println(orders.placeOrder("book"));
        try {
            orders.placeOrder(" ");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
        orders.placeAll(List.of("pen", "ink"));
        System.out.println("Orders: " + controller.listOrders());
    }
}
