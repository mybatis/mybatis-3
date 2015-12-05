package org.apache.ibatis.submitted.emptycollection;

import java.util.List;

public class TodoLists {
  
  @Override
  public String toString() {
    return "TodoLists [id=" + id + ", todoItems=" + todoItems + "]";
  }

  private int id;

  private List<TodoItem> todoItems;
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<TodoItem> getTodoItems() {
    return todoItems;
  }

  public void setTodoItems(List<TodoItem> todoItems) {
    this.todoItems = todoItems;
  }

}
