package com.ibatis.jpetstore.persistence.iface;

import com.ibatis.jpetstore.domain.Category;

import java.util.List;

public interface CategoryDao {

  List getCategoryList();

  Category getCategory(String categoryId);

}
