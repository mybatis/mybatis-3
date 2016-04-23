package org.apache.ibatis.mapping.Dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

/**
 * 
 * @author wenlong.liu
 *
 */
public  class MysqlPageDialect extends PageDialect {
	
	public MysqlPageDialect(MappedStatement mappedStatement, BoundSql boundSql,PageBounds pageBounds){
		super(mappedStatement, boundSql, pageBounds);
	}
	
	
	@Override
	public String bulidListSql() {
		StringBuffer buffer = new StringBuffer(this.boundSql.getSql());
		buffer.append(" LIMIT ?, ?");
		return buffer.toString();
	}
	
	
	
	@Override
	public String bulidCountSql() {
		SQLSelectStatement statement = (SQLSelectStatement) new MySqlStatementParser(this.boundSql.getSql()).parseStatement();
		MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock)statement.getSelect().getQuery();
		mysqlSelectQuery.setOrderBy(null);
		SQLSelectItem count = new SQLSelectItem();
        SQLAggregateExpr countExp = new SQLAggregateExpr("COUNT");
        count.setExpr(countExp);
        countExp.getArguments().add(new SQLAllColumnExpr());
		mysqlSelectQuery.getSelectList().clear();
		mysqlSelectQuery.getSelectList().add(count);
		return statement.toString();
	}

	@Override
	protected void setAdditionalParameter(BoundSql boundSql) {
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getOffset()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getLimit()));
	}
}
