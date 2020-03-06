/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.executor.resultset.nested;

import java.util.List;

public class RankItem {
  private Subject subject;
  private int score;

  private List<Subject> children;

  public Subject getSubject() {
    return subject;
  }

  public void setSubject(Subject subject) {
    this.subject = subject;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public List<Subject> getChildren() {
    return children;
  }

  public void setChildren(List<Subject> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return "RankItem{" +
      "subject=" + subject +
      ", score=" + score +
      ", children=" + children +
      '}';
  }
}
