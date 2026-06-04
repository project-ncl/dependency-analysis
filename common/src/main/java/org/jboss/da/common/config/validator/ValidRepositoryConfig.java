package org.jboss.da.common.config.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = RepositoryConfigValidator.class)
@Documented
public @interface ValidRepositoryConfig {

    String message() default "Repository config is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
