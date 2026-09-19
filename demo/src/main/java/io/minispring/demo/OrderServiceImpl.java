package io.minispring.demo;

import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Transactional;
import io.minispring.container.event.ApplicationEventPublisher;
import java.util.List;

@Component
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final AuditService audit;
    private final ApplicationEventPublisher events;

    public OrderServiceImpl(OrderRepository repository, AuditService audit, ApplicationEventPublisher events) {
        this.repository = repository;
        this.audit = audit;
        this.events = events;
    }

    @Override
    @Transactional
    public String placeOrder(String item) {
        if (item.isBlank()) {
            throw new IllegalArgumentException("item must not be blank");
        }
        repository.save(item);
        audit.record("order placed: " + item);
        events.publishEvent(new OrderPlacedEvent(this, item));
        return "Order placed: " + item;
    }

    /** Calls placeOrder on "this", which bypasses the proxy: no transaction starts. */
    @Override
    public void placeAll(List<String> items) {
        items.forEach(this::placeOrder);
    }

    @Override
    public List<String> orders() {
        return repository.findAll();
    }
}
