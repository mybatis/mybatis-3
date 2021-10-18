package org.apache.ibatis.type;

/**
 * Interface for enums that specify alias - string value which will be saved in database instead of <code>name()</code>
 * <p>
 * <b>Usage:</b>
 * <pre>
 *  enum MyEnum implements EnumAlias {
 *     JavaScript("JS"),
 *     TypeScript("TS"),
 *     Kubernetes("K8S");
 *
 *     private final String fullName;
 *
 *     //...
 *
 *     &#064;Override
 *     public String getAlias() {
 *       return fullName;
 *     }
 *   }
 *   </pre>
 * </p>
 *
 * @author Konstantin Parakhin
 */
public interface EnumAlias {
  /**
   * @return alias for enum value which will be saved in DB
   */
  String getAlias();
}
