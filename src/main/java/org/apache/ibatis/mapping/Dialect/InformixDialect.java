package org.apache.ibatis.mapping.Dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;

/**
 * 
 * @author wenlong.liu
 *
 */
public  class InformixDialect extends PageDialect {
	
	public InformixDialect(MappedStatement mappedStatement, BoundSql boundSql,PageBounds pageBounds){
		super(mappedStatement, boundSql, pageBounds);
	}
	
	
	@Override
	public String bulidListSql() {
		StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql().length() + 40);
        sqlBuilder.append("select skip ? first ? * from ( ");
        sqlBuilder.append(boundSql.getSql());
        sqlBuilder.append(" )");
        return sqlBuilder.toString();
	}
	
	
	
//	@Override
//	public String bulidCountSql() {
//		SQLSelectStatement statement = (SQLSelectStatement) new MySqlStatementParser(this.boundSql.getSql()).parseStatement();
//		MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock)statement.getSelect().getQuery();
//		mysqlSelectQuery.setOrderBy(null);
//		SQLSelectItem count = new SQLSelectItem();
//        SQLAggregateExpr countExp = new SQLAggregateExpr("COUNT");
//        count.setExpr(countExp);
//        countExp.getArguments().add(new SQLAllColumnExpr());
//		mysqlSelectQuery.getSelectList().clear();
//		mysqlSelectQuery.getSelectList().add(count);
//		return statement.toString();
//	}

	@Override
	protected void setAdditionalParameter(BoundSql boundSql) {
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getOffset()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getLimit()));
	}
}
