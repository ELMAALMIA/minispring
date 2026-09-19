package io.minispring.demo;

import java.util.List;

public interface OrderRepository {

    void save(String item);

    List<String> findAll();
}
