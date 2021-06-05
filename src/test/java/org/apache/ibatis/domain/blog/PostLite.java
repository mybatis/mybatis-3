/*
 *    Copyright 2009-2021 the original author or authors.
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

public class PostLite {
    private PostLiteId theId;
    private int blogId;

    public PostLite() {
    }

    public PostLite(PostLiteId aId, int aBlogId) {
        blogId = aBlogId;
        theId = aId;
    }

    public void setId(PostLiteId aId) {
        theId = aId;
    }

    public void setBlogId(int aBlogId) {
        blogId = aBlogId;
    }

    public PostLiteId getId() {
        return theId;
    }

    public int getBlogId() {
        return blogId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PostLite that = (PostLite) o;

        if (blogId != that.blogId) {
            return false;
        }
        if (theId != null ? !theId.equals(that.theId) : that.theId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int myresult = theId != null ? theId.hashCode() : 0;
        myresult = 31 * myresult + blogId;
        return myresult;
    }
}
