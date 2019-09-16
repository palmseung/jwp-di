package core.di.scanner;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import core.annotation.ComponentScan;
import core.annotation.Configuration;
import core.di.factory.BeanFactory;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationBeanScanner {

    private final Set<String> basePackages = new HashSet<>();
    private final BeanFactory beanFactory;

    public ConfigurationBeanScanner(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void register(Class<?> configClass) {
        Preconditions.checkArgument(configClass.isAnnotationPresent(Configuration.class),
                "Configuration 클래스만 가능합니다. class : [%s]", configClass);

        Set<Class<?>> configurationClasses = scanConfigurationClasses(configClass);
        basePackages.addAll(getDeclareBasePackages(configurationClasses));
        beanFactory.addAllBeanClasses(configurationClasses);
    }

    private Set<Class<?>> scanConfigurationClasses(Class<?> configClass) {
        String[] basePackages = getBasePackages(configClass);
        if (basePackages.length == 0) {
            return ImmutableSet.of(configClass);
        }
        Reflections reflections = new Reflections(basePackages);
        return reflections.getTypesAnnotatedWith(Configuration.class);
    }

    private Set<String> getDeclareBasePackages(Set<Class<?>> configurationClasses) {
        return configurationClasses.stream().map(this::getBasePackages)
                .filter(basePackages -> basePackages.length != 0)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    private String[] getBasePackages(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(ComponentScan.class)) {
            return new String[0];
        }

        ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
        String[] basePackages = componentScan.basePackages();
        if (basePackages.length == 0) {
            return getValue(componentScan.value(), configClass);
        }
        return basePackages;
    }

    private String[] getValue(String[] value, Class<?> configClass) {
        if (value.length != 0) {
            return value;
        }
        return new String[] {configClass.getPackage().getName()};
    }

    public String[] getBasePackages() {
        return basePackages.toArray(new String[0]);
    }

}
