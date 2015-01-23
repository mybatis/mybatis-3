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
package org.apache.ibatis.domain.blog;

import java.io.Serializable;

public class ComplexImmutableAuthor implements Serializable {
  private final ComplexImmutableAuthorId theComplexImmutableAuthorId;
  protected final String bio;
  protected final Section favouriteSection;

  public ComplexImmutableAuthor(ComplexImmutableAuthorId aComplexImmutableAuthorId, String bio, Section section) {
    theComplexImmutableAuthorId = aComplexImmutableAuthorId;
    this.bio = bio;
    this.favouriteSection = section;
  }

  public ComplexImmutableAuthorId getComplexImmutableAuthorId() {
    return theComplexImmutableAuthorId;
  }

  public String getBio() {
    return bio;
  }

  public Section getFavouriteSection() {
    return favouriteSection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ComplexImmutableAuthor that = (ComplexImmutableAuthor) o;

    if (bio != null ? !bio.equals(that.bio) : that.bio != null) {
      return false;
    }
    if (favouriteSection != that.favouriteSection) {
      return false;
    }
    if (theComplexImmutableAuthorId != null ? !theComplexImmutableAuthorId.equals(that.theComplexImmutableAuthorId) : that.theComplexImmutableAuthorId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int myresult = theComplexImmutableAuthorId != null ? theComplexImmutableAuthorId.hashCode() : 0;
    myresult = 31 * myresult + (bio != null ? bio.hashCode() : 0);
    myresult = 31 * myresult + (favouriteSection != null ? favouriteSection.hashCode() : 0);
    return myresult;
  }
}
