package io.minispring.container.bean;

import io.minispring.container.exception.BeanDefinitionException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Holds the bean definitions, indexed by name and kept in registration order. */
public final class BeanRegistry {

    private final Map<String, BeanDefinition> definitions = new LinkedHashMap<>();

    public void register(BeanDefinition definition) {
        BeanDefinition existing = definitions.putIfAbsent(definition.name(), definition);
        if (existing != null) {
            throw new BeanDefinitionException("Duplicate bean name '%s': it is used by both %s and %s. Give one of them an explicit name with @Component(\"...\")."
                    .formatted(definition.name(), existing.type().getName(), definition.type().getName()));
        }
    }

    public Optional<BeanDefinition> find(String name) {
        return Optional.ofNullable(definitions.get(name));
    }

    public boolean contains(String name) {
        return definitions.containsKey(name);
    }

    /** Returns the definitions whose type can be assigned to {@code type}, in registration order. */
    public List<BeanDefinition> definitionsOfType(Class<?> type) {
        return definitions.values().stream()
                .filter(definition -> type.isAssignableFrom(definition.type()))
                .toList();
    }

    public List<BeanDefinition> definitions() {
        return List.copyOf(definitions.values());
    }
}
