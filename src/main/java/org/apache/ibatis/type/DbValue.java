package org.apache.ibatis.type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that specify property of enum to be used for database I/O.
 * See also: {@link EnumTypeHandler}
 * <p>
 * <b>How to use:</b>
 * <pre>
 * public enum ExampleEnum {
 *   EX1,EX2;
 *
 *  {@literal @DbValue}
 *   public String getDbCode() {
 *     return this.name().toLowerCase();
 *   }
 * }
 * </pre>
 * @author umbum
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DbValue {
}
