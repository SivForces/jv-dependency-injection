package mate.academy.lib;

import java.lang.reflect.Field;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public static Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = getImplementation(interfaceClazz);
        checkComponentAnnotation(implementationClass);
        Object instance = createInstance(implementationClass);
        injectDependencies(instance);
        return instance;
    }

    private static Class<?> getImplementation(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return clazz;
        }
        if (clazz.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        }
        if (clazz.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        }
        if (clazz.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        }

        throw new RuntimeException("There is no implementation for " + clazz.getName());
    }

    private static void checkComponentAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
    }

    public static <T> T createInstance(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance of "
                    + clazz.getName(), e);
        }
    }

    private static void injectDependencies(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {

            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> dependencyType = field.getType();

                Object dependency = getInstance(dependencyType);

                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot inject into " + field.getName(), e);
                }
            }
        }
    }

    private static void injectFields(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> dependencyType = field.getType();

                Object dependency = getInstance(dependencyType);

                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject field: " + field, e);
                }
            }
        }
    }
}
