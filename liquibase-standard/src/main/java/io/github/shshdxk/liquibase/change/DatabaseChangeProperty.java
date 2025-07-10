package io.github.shshdxk.liquibase.change;

import io.github.shshdxk.liquibase.serializer.ChangeLogSerializer;
import io.github.shshdxk.liquibase.ChecksumVersion;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.serializer.LiquibaseSerializable;

import java.lang.annotation.*;

/**
 * Annotation used by {@link AbstractChange} to declare {@link ChangeParameterMetaData} information.
 * The annotation should be placed on the read method.
 * This annotation should not be checked for outside AbstractChange, if any code is trying to determine the
 * metadata provided by this annotation, it should get it from
 * {@link ChangeFactory#getChangeMetaData(Change)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(DatabaseChangeProperties.class)
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

    /**
     * The checksum version that this annotation applies to. This can be omitted, and it is assumed that the
     * annotation applies to all checksum versions. If a version is applied to some, but not all of the
     * {@link DatabaseChangeProperty} annotations on a particular property, the most specific matching annotation
     * is selected. For example, if a particular property has:
     *
     * <code>
     *     @DatabaseChangeProperty(isChangeProperty = false, version = {ChecksumVersions.V8})
     *     @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE)
     * </code>
     *
     * and checksum calculation for version 8 is requested, the first annotation is used. If any other checksum version
     * is requested, the second annotation is used.
     */
    ChecksumVersion[] version() default {};

    String[] alternatePropertyNames() default {};
}
