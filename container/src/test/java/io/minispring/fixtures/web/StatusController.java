package io.minispring.fixtures.web;

import io.minispring.container.web.GetMapping;
import io.minispring.container.web.RestController;

@RestController
public class StatusController {

    @GetMapping("/status")
    public String status() {
        return "up";
    }
}
