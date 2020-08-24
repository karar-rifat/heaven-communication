package com.aastha.communication.mapping;

import com.aastha.communication.message.JmsTypeId;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Factory for a JMS type mapping used by Jackson. Discoders all types annotated and collect them
 * as a mapping.
 *
 * @see JmsTypeId
 */
public class TypeIdMappingFactory extends ClassPathScanningCandidateComponentProvider implements
    FactoryBean<TypeIdMapping> {

    private static List<String> DEFAULT_BASE_PACKAGES =
        asList("com.aastha.communication");

    private List<String> basePackages;

    public TypeIdMappingFactory(String... basePackages) {
        super(false);
        super.addIncludeFilter(new AnnotationTypeFilter(JmsTypeId.class));
        this.basePackages = basePackages.length == 0 ? DEFAULT_BASE_PACKAGES : asList(basePackages);
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public TypeIdMapping getObject() {
        return new TypeIdMapping(basePackages.stream()
            .map(this::findCandidateComponents).flatMap(Collection::stream)
            .map(def -> getType(def.getBeanClassName()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toMap(this::getTypeId, identity())));
    }

    @Override
    public Class<?> getObjectType() {
        return TypeIdMapping.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private String getTypeId(Class<?> type) {
        return type.getAnnotation(JmsTypeId.class).value();
    }

    private Optional<Class<?>> getType(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException unknown) {
            return Optional.empty();
        }
    }
}
