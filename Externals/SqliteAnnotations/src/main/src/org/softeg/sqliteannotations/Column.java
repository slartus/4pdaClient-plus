package org.softeg.sqliteannotations;



import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    boolean isPrimaryKey() default false;
    boolean isAutoincrement() default false;
    /** Column type. */
    String type() default "TEXT";
    /** Column name. */
    String name();
}
