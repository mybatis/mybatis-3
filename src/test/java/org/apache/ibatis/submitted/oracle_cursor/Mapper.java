/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.oracle_cursor;

import java.util.List;

public interface Mapper {

  List<Author> selectImplicitCursors_Statement();

  List<Author> selectImplicitCursors_Prepared();

  List<Author> selectImplicitCursors_Callable();

  List<Author> selectNestedCursor_Statement();

  List<Author> selectNestedCursor_Prepared();

  List<Author> selectNestedCursor_Callable();

  List<Author> selectNestedCursor_Automap();

  List<Author> selectNestedCursorConstructorCollection();

  List<Author> selectNestedCursorTypeHandler();

  List<Author> selectNestedCursorTypeHandlerConstructor();

  List<Author2> selectNestedCursorOfStrings();

  List<Book2> selectNestedCursorAssociation();

  List<Book2> selectNestedCursorConstructorAssociation();

}
