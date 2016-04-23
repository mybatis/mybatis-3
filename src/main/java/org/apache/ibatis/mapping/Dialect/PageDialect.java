package org.apache.ibatis.mapping.Dialect;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.PageBounds;

/**
 * 
 * @author wenlong.liu
 *
 */
public abstract class PageDialect{

	protected PageBounds pageBounds;
	protected BoundSql boundSql;
	protected MappedStatement mappedStatement;
	protected Configuration configuration;
	
	/**
	 * 
	 * @param mappedStatement
	 * @param boundSql
	 * @param pageBounds
	 */
	public PageDialect(MappedStatement mappedStatement, BoundSql boundSql,PageBounds pageBounds){
		this.boundSql = boundSql;
		this.configuration = mappedStatement.getConfiguration();
		this.mappedStatement = mappedStatement;
		this.pageBounds = pageBounds;
	}
	/**
	 * 
	 * @return
	 */
	public BoundSql getListBoundSql(){
		String sql = bulidListSql();
		BoundSql boundSql = copyFromBoundSql(mappedStatement, this.boundSql, sql, this.boundSql.getParameterMappings(), this.boundSql.getParameterObject());
		boundSql.getParameterMappings().add(new ParameterMapping.Builder(configuration, "autoOffset", Integer.class).build());
		boundSql.getParameterMappings().add(new ParameterMapping.Builder(configuration, "autoLimit", Integer.class).build());
		setAdditionalParameter(boundSql);
		return boundSql;
	}
	

	/**
	 * 
	 * @return
	 */
	public BoundSql getCountBoundSql(){
		String sql = bulidCountSql();
		BoundSql boundSql = copyFromBoundSql(mappedStatement, this.boundSql, sql, this.boundSql.getParameterMappings(), this.boundSql.getParameterObject());
		return boundSql;
	}
	
	
	/**
	 * 
	 * @param ms
	 * @param boundSql
	 * @return
	 */
	public MappedStatement copyFromMappedStatement(MappedStatement ms, BoundSql boundSql) {
		List<ResultMap> resultMaps = new ArrayList<ResultMap>();
		MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, ms.getId(), new BoundSqlSource(boundSql),ms.getSqlCommandType())
        .resource(ms.getResource())
        .fetchSize(ms.getFetchSize())
        .timeout(ms.getTimeout())
        .statementType(StatementType.PREPARED)
        .databaseId(ms.getDatabaseId())
        .lang(ms.getLang())
        .resultMaps(resultMaps)
        .resultSetType(ms.getResultSetType())
        .flushCacheRequired(false)
        .useCache(false)
        .cache(ms.getCache());
		resultMaps.add(new ResultMap.Builder(configuration,ms.getId() + "-Inline",Integer.class,new ArrayList<ResultMapping>(),null).build());
		return statementBuilder.build();
	}
	
	/**
	 * 
	 * @param dialect
	 * @param mappedStatement
	 * @param boundSql
	 * @param pageBounds
	 * @return
	 */
	public static PageDialect createStrategy(Dialect dialect, MappedStatement mappedStatement, BoundSql boundSql,PageBounds pageBounds)
	{
		switch(dialect){
			case mysql : 
				return new MysqlPageDialect(mappedStatement, boundSql, pageBounds);
			case oracle : 
				return new OraclePageDialect(mappedStatement, boundSql, pageBounds);
			case sqlserver2012 : 
				return new SqlServer2012Dialect(mappedStatement, boundSql, pageBounds);
			case db2 : 
				return new Db2Dialect(mappedStatement, boundSql, pageBounds);
			case postgresql : 
				return new PostgreSQLDialect(mappedStatement, boundSql, pageBounds);
			case informix : 
				return new InformixDialect(mappedStatement, boundSql, pageBounds);
			case hsqldb : 
				return new PostgreSQLDialect(mappedStatement, boundSql, pageBounds);
			case h2 : 
				return new PostgreSQLDialect(mappedStatement, boundSql, pageBounds);
			default :
				throw new java.lang.UnsupportedOperationException();
		}
	}

	protected abstract String  bulidCountSql();
	
	
	protected abstract String  bulidListSql();
	
	
	protected abstract void  setAdditionalParameter(BoundSql boundSql);
	
	
	/**
	 * 
	 * @param ms
	 * @param boundSql
	 * @param sql
	 * @param parameterMappings
	 * @param parameter
	 * @return
	 */
	private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql, List<ParameterMapping> parameterMappings,Object parameter) {
		List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>(parameterMappings);
		BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, newParameterMappings, parameter);
		return newBoundSql;
	}
	
	/**
	 * 
	 * @author pc
	 *
	 */
	private static class BoundSqlSource implements SqlSource {
		BoundSql boundSql;
		public BoundSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}
}
