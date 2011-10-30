package domain.blog;

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
