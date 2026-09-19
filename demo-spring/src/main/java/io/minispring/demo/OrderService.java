package io.minispring.demo;

import java.util.List;

public interface OrderService {

    String placeOrder(String item);

    void placeAll(List<String> items);

    List<String> orders();
}
