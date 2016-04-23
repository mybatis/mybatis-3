package org.apache.ibatis.mapping.Dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;

/**
 * 
 * @author wenlong.liu
 *
 */
public class SqlServer2012Dialect  extends PageDialect {

	public SqlServer2012Dialect(MappedStatement mappedStatement, BoundSql boundSql, PageBounds pageBounds) {
		super(mappedStatement, boundSql, pageBounds);
	}


	@Override
	public String bulidListSql() {
		StringBuilder sqlBuilder = new StringBuilder(this.boundSql.getSql().length() + 14);
        sqlBuilder.append(this.boundSql.getSql());
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        return sqlBuilder.toString();
	}
	
	@Override
	public String bulidCountSql() {
		SQLSelectStatement statement = new SQLServerStatementParser(this.boundSql.getSql()).parseSelect();
		SQLSelect sqlSelect = statement.getSelect();
		SQLServerSelectQueryBlock selectQuery = (SQLServerSelectQueryBlock) sqlSelect.getQuery();
		sqlSelect.setOrderBy(null);
		SQLSelectItem count = new SQLSelectItem();
        SQLAggregateExpr countExp = new SQLAggregateExpr("COUNT");
        count.setExpr(countExp);
        countExp.getArguments().add(new SQLAllColumnExpr());
        selectQuery.getSelectList().clear();
        selectQuery.getSelectList().add(count);
		return statement.toString();
	}


	@Override
	protected void setAdditionalParameter(BoundSql boundSql) {
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getOffset()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getLimit()));
	}

}
