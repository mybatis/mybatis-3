package bedpotato.util;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisUtil {
	private static SqlSessionFactory sessionFactory;
	static {
		try {
			Reader resource = Resources.getResourceAsReader("bedpotato/configuration.xml");
			sessionFactory = new SqlSessionFactoryBuilder().build(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SqlSession getSqlSession() {
		return sessionFactory.openSession();
	}

	public static SqlSession getBatchSqlSession() {
		return sessionFactory.openSession(ExecutorType.BATCH, false);
	}
}
