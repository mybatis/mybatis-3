package bedpotato;

import java.io.IOException;

import org.apache.ibatis.session.SqlSession;

import bedpotato.data.UserMapper;
import bedpotato.model.User;
import bedpotato.util.MybatisUtil;

public class App {
	public static void main(String[] args) throws IOException {
		SqlSession session = MybatisUtil.getSqlSession();
		UserMapper mapper = session.getMapper(UserMapper.class);
		User user = new User(1, "lialun", "lialun@bedpotato.com", "123456");
		mapper.insert(user);
		session.commit();
		session.close();
	}
}
