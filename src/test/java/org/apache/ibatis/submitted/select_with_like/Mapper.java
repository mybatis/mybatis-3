package org.apache.ibatis.submitted.select_with_like;

import org.apache.ibatis.annotations.Insert;

interface Mapper {

    @Insert({ "<script>", "select * from test where name like #{name, like = RIGHT}", "</script>" })
    void selectWithLikeRight(String name);

    @Insert({ "<script>", "select * from test where name like #{name}", "</script>" })
    void selectWithNoLike(String name);

    @Insert({ "<script>", "select * from test where name like #{name, like = placeHolder}", "</script>" })
    void selectWithOodLike(String name);

  }
