package org.apache.ibatis.submitted.automappingcache;

public class InnerEntity {
    private Integer id;
    private Integer complexCalculated;
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getComplexCalculated() {
        return complexCalculated;
    }

    public void setComplexCalculated(Integer complexCalculated) {
        this.complexCalculated = complexCalculated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
