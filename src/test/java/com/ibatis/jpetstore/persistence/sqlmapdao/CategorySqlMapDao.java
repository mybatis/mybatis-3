package com.ibatis.jpetstore.persistence.sqlmapdao;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.domain.Category;
import com.ibatis.jpetstore.persistence.iface.CategoryDao;

import java.util.List;

public class CategorySqlMapDao extends BaseSqlMapDao implements CategoryDao {

  public CategorySqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  public List getCategoryList() {
    return queryForList("getCategoryList", null);
  }

  public Category getCategory(String categoryId) {
    return (Category) queryForObject("getCategory", categoryId);
  }

}
