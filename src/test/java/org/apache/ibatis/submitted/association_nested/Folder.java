package org.apache.ibatis.submitted.association_nested;

/**
 * @author Loïc Guerrin <guerrin@fullsix.com>
 */
public class Folder {

   public Long id;
   public String name;

   @Override
   public String toString() {
     return name;
   }


}
