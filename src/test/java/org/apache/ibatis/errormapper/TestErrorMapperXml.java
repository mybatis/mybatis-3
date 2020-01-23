package org.apache.ibatis.errormapper;

import org.apache.ibatis.domain.blog.Author;

import java.util.List;

/**
 * @author chengdu
 * @date 2020/1/24
 */
public interface TestErrorMapperXml {
  int insertAuthorList(List<Author> author);
}
