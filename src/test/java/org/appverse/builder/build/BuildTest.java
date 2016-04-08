package org.appverse.builder.build;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by panthro on 08/03/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //can use in method only.
public @interface BuildTest {
    TestType testType();

    enum TestType {DOCKER_TEST, DOCKGRANT_TEST}
}
