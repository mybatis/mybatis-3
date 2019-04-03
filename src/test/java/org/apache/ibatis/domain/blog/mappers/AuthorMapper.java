/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.domain.blog.mappers;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.session.ResultHandler;

public interface AuthorMapper {

  List<Author> selectAllAuthors();

  Set<Author> selectAllAuthorsSet();

  Vector<Author> selectAllAuthorsVector();

  LinkedList<Author> selectAllAuthorsLinkedList();

  Author[] selectAllAuthorsArray();

  void selectAllAuthors(ResultHandler handler);

  Author selectAuthor(int id);

  LinkedHashMap<String, Object> selectAuthorLinkedHashMap(int id);

  void selectAuthor(int id, ResultHandler handler);

  @Select("select")
  void selectAuthor2(int id, ResultHandler handler);

  void insertAuthor(Author author);

  int deleteAuthor(int id);

  int updateAuthor(Author author);

}
