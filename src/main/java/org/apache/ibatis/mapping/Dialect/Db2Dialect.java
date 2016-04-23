package org.apache.ibatis.mapping.Dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.db2.ast.stmt.DB2SelectQueryBlock;
import com.alibaba.druid.sql.dialect.db2.parser.DB2StatementParser;

/**
 * 
 * @author wenlong.liu
 *
 */
public class Db2Dialect extends PageDialect {

	

	public Db2Dialect(MappedStatement mappedStatement, BoundSql boundSql, PageBounds pageBounds) {
		super(mappedStatement, boundSql, pageBounds);
	}

	@Override
	public String bulidListSql() {
		StringBuilder sqlBuilder = new StringBuilder(boundSql.getSql().length() + 120);
        sqlBuilder.append("select * from (select tmp_page.*,rownumber() over() as row_id from ( ");
        sqlBuilder.append(boundSql.getSql());
        sqlBuilder.append(" ) as tmp_page) where row_id between ? and ?");
        return sqlBuilder.toString();
	}

	@Override
	public String bulidCountSql() {
		SQLSelectStatement statement = new DB2StatementParser(this.boundSql.getSql()).parseSelect();
		SQLSelect sqlSelect = statement.getSelect();
		DB2SelectQueryBlock selectQuery = (DB2SelectQueryBlock) sqlSelect.getQuery();
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
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getBeginRow()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getEndRow()));
	}

}
