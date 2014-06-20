package org.apache.ibatis.submitted.association_nested;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Loïc Guerrin <guerrin@fullsix.com>
 */
public interface FolderMapper {

  List<FolderFlatTree> findWithSubFolders(@Param("name") String name);

}
