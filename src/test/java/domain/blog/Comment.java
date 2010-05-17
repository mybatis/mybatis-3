package domain.blog;

public class Comment {

  private int id;
  private Post post;
  private String name;
  private String comment;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Post getPost() {
    return post;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String toString() {
    return "Comment: " + id + " : " + name + " : " + comment;
  }
}
