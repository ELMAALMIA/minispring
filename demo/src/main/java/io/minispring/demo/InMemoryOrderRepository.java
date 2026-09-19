package io.minispring.demo;

import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final List<String> orders = new ArrayList<>();

    @PostConstruct
    void open() {
        System.out.println("Repository ready");
    }

    @Override
    public void save(String item) {
        orders.add(item);
    }

    @Override
    public List<String> findAll() {
        return List.copyOf(orders);
    }

    @PreDestroy
    void close() {
        System.out.println("Repository closed");
    }
}
