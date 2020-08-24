package com.aastha.communication.message;

import com.aastha.communication.mapping.TypeIdMappingFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a type id for JSON JMS messages.
 *
 * @see TypeIdMappingFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JmsTypeId {
    String value();
}
