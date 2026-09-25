package io.minispring.autoconfigure.condition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

/** Works out which bean types a condition should look for. */
final class SearchedTypes {

    private SearchedTypes() {
    }

    /**
     * The declared types, or, when none is given, the type the annotated element is about: the
     * return type of a {@code @Bean} method, or the class itself.
     */
    static List<Class<?>> of(Class<?>[] declared, AnnotatedElement element) {
        if (declared.length > 0) {
            return List.of(declared);
        }
        return element instanceof Method method ? List.of(method.getReturnType()) : List.of((Class<?>) element);
    }

    static String describe(List<Class<?>> types) {
        return types.stream().map(Class::getSimpleName).reduce((first, second) -> first + ", " + second).orElse("");
    }
}
