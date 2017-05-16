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

	public  String  bulidCountSql(){
		String sql = this.boundSql.getSql();
		sql = sql.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ");
		java.util.Stack<String> keyStack = new java.util.Stack<String>();
		String [] strArr = sql.trim().split("\\s+");
		int orderbyIndex = 0;
		boolean isAppend = false;
		boolean isUnion = false;
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<strArr.length; i++)
		{
			String word = strArr[i];
			if("select".equalsIgnoreCase(word)){
				if(keyStack.isEmpty()){
					buffer.append(word).append(" count(*) as total ");
					isAppend = false;
				}
				keyStack.push(word);
			}else if("from".equalsIgnoreCase(word)){
				keyStack.pop();
				if(keyStack.isEmpty()) isAppend = true;
			}else if("(".equalsIgnoreCase(word))
				keyStack.push(word);
			else if(")".equalsIgnoreCase(word))
				keyStack.pop();
			else if("order".equalsIgnoreCase(word) && "by".equalsIgnoreCase(strArr[i+1])){
				if(keyStack.isEmpty()) isAppend = false;
			}else if(orderbyIndex==0 && "union".equalsIgnoreCase(word)){
				if(keyStack.isEmpty()) {
					isAppend = true;
					isUnion = true;
				}
			}
			
			if(isAppend) buffer.append(word).append(" ");
		}
		//delete last space
		buffer.deleteCharAt(buffer.length()-1);
		if(isUnion)
		{ 
			buffer.insert(0, "select sum(total) from (");
			buffer.append(")");
		}
		return buffer.toString();
	}
	
	
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
		List<ParameterMapping> newParameterMappings ;
		if(parameterMappings==null)
			newParameterMappings = new ArrayList<ParameterMapping>();
		else
			newParameterMappings = new ArrayList<ParameterMapping>(parameterMappings);
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
