/*
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
package org.apache.ibatis.submitted.constructor_columnprefix;

import java.util.List;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  List<Article> getArticles();

  @ConstructorArgs({
      @Arg(id = true, resultMap = "keyRM", columnPrefix = "key_", javaType = EntityKey.class),
      @Arg(column = "name", javaType = String.class),
      @Arg(resultMap = "authorRM", columnPrefix = "author_", javaType = Author.class),
      @Arg(resultMap = "authorRM", columnPrefix = "coauthor_", javaType = Author.class),
  })
  @Select({
      "select id key_id, name, author.id author_id, author.name author_name,",
      "  coauthor.id coauthor_id, coauthor.name coauthor_name",
      "from articles",
      "left join authors author on author.id = articles.author_id",
      "left join authors coauthor on coauthor.id = articles.coauthor_id",
      "order by articles.id"
  })
  List<Article> getArticlesAnno();

}
