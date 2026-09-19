package io.minispring.demo;

import org.springframework.stereotype.Component;

@Component
public class AuditService {

    public void record(String event) {
        System.out.println("AUDIT    " + event);
    }
}
