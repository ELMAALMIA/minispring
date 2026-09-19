package io.minispring.fixtures.scan;

import io.minispring.container.annotation.Component;

public class Holder {

    @Component
    public static class Nested {
    }

    @Component
    public class Inner {
    }
}
