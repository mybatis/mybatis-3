package domain.blog.mappers;

import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

public interface BlogMapper {

  List<Map> selectAllPosts();

  List<Map> selectAllPosts(RowBounds rowBounds);

  List<Map> selectAllPosts(RowBounds rowBounds, Object param);

}
