package io.github.shshdxk.liquibase.change;

import io.github.shshdxk.liquibase.serializer.ChangeLogSerializer;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.serializer.LiquibaseSerializable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by {@link AbstractChange } to declare {@link ChangeParameterMetaData} information.
 * The annotation should be placed on the read method.
 * This annotation should not be checked for outside AbstractChange, if any code is trying to determine the
 * metadata provided by this annotation, it should get it from
 * {@link ChangeFactory#getChangeMetaData(Change)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DatabaseChangeProperty {

    /**
     * Value to put into {@link ChangeParameterMetaData#getDescription()}
     */
    String description() default "";

    /**
     * Value to put into {@link ChangeParameterMetaData#getExampleValue(Database)}
     */
    String exampleValue() default "";

    /**
     * Value to put into {@link ChangeParameterMetaData#getSince()}
     */
    String since() default "";

    /**
     * If false, this field or method will not be included in {@link ChangeParameterMetaData}
     */
    boolean isChangeProperty() default true;

    /**
     * Value to put into {@link ChangeParameterMetaData#getRequiredForDatabase()}
     */
    String[] requiredForDatabase() default ChangeParameterMetaData.COMPUTE;

    /**
     * Value to put into {@link ChangeParameterMetaData#getSupportedDatabases()}
     */
    String[] supportsDatabase() default ChangeParameterMetaData.COMPUTE;

    /**
     * Value to put into {@link ChangeParameterMetaData#getMustEqualExisting()}
     */
    String mustEqualExisting() default "";

    /**
     * Format to use when serializing this Change via a {@link ChangeLogSerializer}.
     */
    LiquibaseSerializable.SerializationType serializationType() default LiquibaseSerializable.SerializationType
        .NAMED_FIELD;
}
