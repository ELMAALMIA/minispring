package io.minispring.demo;

import io.minispring.container.annotation.Component;

@Component
public class AuditService {

    public void record(String event) {
        System.out.println("AUDIT    " + event);
    }
}
