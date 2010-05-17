package domain.misc.generics;

public class GenericConcrete extends GenericSubclass implements GenericInterface<Long> {
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(String id) {
    this.id = Long.valueOf(id);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setId(Integer id) {
    this.id = (long) id;
  }
}
