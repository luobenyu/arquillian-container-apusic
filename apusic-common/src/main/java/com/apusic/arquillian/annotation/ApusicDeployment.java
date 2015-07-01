package com.apusic.arquillian.annotation;

import com.apusic.arquillian.container.ArchiveDeployConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Patrick Huang
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ApusicDeployment {

    String name();
    String baseContext();
    String virtualHost() default ArchiveDeployConfiguration.DEFAULT_VIRTUALHOST;
    String startType() default ArchiveDeployConfiguration.DEFAULT_STARTTYPE;
    boolean globalSession();
}
