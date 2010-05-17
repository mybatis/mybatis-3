package domain.blog.mappers;

import domain.blog.Author;

import java.util.List;

public interface AuthorMapper {

  List selectAllAuthors();

  Author selectAuthor(int id);

  void insertAuthor(Author author);

  int deleteAuthor(int id);

  int updateAuthor(Author author);

}



