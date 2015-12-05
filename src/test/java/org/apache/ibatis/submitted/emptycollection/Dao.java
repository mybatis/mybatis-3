package org.apache.ibatis.submitted.emptycollection;

import java.util.List;

interface Dao {
    List<TodoLists> selectWithEmptyList();
    List<TodoLists> selectWithNonEmptyList();
    List<TodoLists> selectWithNonEmptyList_noCollectionId();
}
