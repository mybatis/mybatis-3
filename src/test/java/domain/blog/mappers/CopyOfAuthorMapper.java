/*
 *    Copyright 2009-2012 the original author or authors.
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
package domain.blog.mappers;

import domain.blog.Author;

import java.util.List;

import org.apache.ibatis.session.ResultHandler;

public interface CopyOfAuthorMapper {

  List selectAllAuthors();

  void selectAllAuthors(ResultHandler handler);

  Author selectAuthor(int id);

  void selectAuthor(int id, ResultHandler handler);

  void insertAuthor(Author author);

  int deleteAuthor(int id);

  int updateAuthor(Author author);

}



