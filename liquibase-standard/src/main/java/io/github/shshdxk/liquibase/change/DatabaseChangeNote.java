package io.github.shshdxk.liquibase.change;

public @interface DatabaseChangeNote {
    String database() default "";
    String notes() default "";
}
