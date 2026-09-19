# minispring

A minimal dependency injection container written from scratch in Java 21. It exists to make Spring's machinery visible.

**It is not a Spring replacement.** It covers component scanning, constructor injection, scopes, lifecycle callbacks and proxy-based `@Transactional`, and nothing else. You should be able to read the whole thing in an afternoon.

## Build

```bash
./mvnw verify
```

The build requires JDK 21. The container module has **zero runtime dependencies**: JUnit and AssertJ are used for tests only.

## Out of scope

These are deliberate choices, not missing features:

- XML configuration
- Field and setter injection (the container supports constructor injection only)
- Conditional auto-configuration
- CGLIB class proxies
- Anything related to a servlet container
