package io.minispring.fixtures.lazy;

import io.minispring.container.annotation.Component;

@Component
public class ExplodingInitializer {

    static {
        failIfInitialized();
    }

    private static void failIfInitialized() {
        throw new IllegalStateException("The scanner must not run static initializers");
    }
}
