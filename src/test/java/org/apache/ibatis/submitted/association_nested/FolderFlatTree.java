/*
 *    Copyright 2009-2015 the original author or authors.
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
