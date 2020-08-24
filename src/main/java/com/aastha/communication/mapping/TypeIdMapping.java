package com.aastha.communication.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * To make services more independent and allow different types handling JSON payload on server
 * and client side.
 *
 * @see org.springframework.jms.support.converter.MappingJackson2MessageConverter#setTypeIdMappings(java.util.Map)
 */
public class TypeIdMapping {
    public static final String JMS_PROPERTY_TYPE_ID = "TypeId";

    private final Map<String, Class<?>> mapping;

    public TypeIdMapping() {
        this(new HashMap<>());
    }

    public TypeIdMapping(Map<String, Class<?>> mapping) {
        this.mapping = mapping;
    }

    public Map<String, Class<?>> getMapping() {
        return mapping;
    }

    public TypeIdMapping addMapping(String id, Class<?> type) {
        mapping.put(id, type);
        return this;
    }
}
