package org.apache.ibatis.submitted.association_nested;

/**
 * @author Loïc Guerrin <guerrin@fullsix.com>
 */
public class FolderFlatTree {

  public Folder root;
  public Folder level1;
  public Folder level2;

  @Override
  public String toString() {
    return root
            + "\n\t" + level1
            + "\n\t\t" + level2;
  }
}
