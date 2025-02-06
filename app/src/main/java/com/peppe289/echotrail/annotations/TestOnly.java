package com.peppe289.echotrail.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is used to mark methods that are only used for testing purposes.
 * Using this annotation, we declare that the method is not part of the public API
 * and shouldn't be used in production code but, only in test code from mockito or
 * other testing frameworks.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS) // Visibile solo a livello di compilazione
public @interface TestOnly {
}
