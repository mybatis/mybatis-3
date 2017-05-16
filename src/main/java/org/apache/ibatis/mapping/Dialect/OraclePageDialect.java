package org.apache.ibatis.mapping.Dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;


/**
 * 
 * @author wenlong.liu
 *
 */
public  class OraclePageDialect extends PageDialect {
	
	public OraclePageDialect(MappedStatement mappedStatement, BoundSql boundSql,PageBounds pageBounds){
		super(mappedStatement, boundSql, pageBounds);
	}
	
	
	@Override
	public String bulidListSql() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT * FROM (SELECT ROWNUM RN, T.* FROM (");
		buffer.append(this.boundSql.getSql());
		buffer.append(") T WHERE ROWNUM <=?) TT WHERE TT.RN >?");
		return buffer.toString();
	}
	
//	@Override
//	public String bulidCountSql() {
//		SQLSelectStatement statement = (SQLSelectStatement) new OracleStatementParser(this.boundSql.getSql()).parseStatement();
//		OracleSelect sqlSelect = (OracleSelect) statement.getSelect();
//		OracleSelectQueryBlock selectQuery = (OracleSelectQueryBlock) sqlSelect.getQuery();
//		sqlSelect.setOrderBy(null);
//		SQLSelectItem count = new SQLSelectItem();
//        SQLAggregateExpr countExp = new SQLAggregateExpr("COUNT");
//        count.setExpr(countExp);
//        countExp.getArguments().add(new SQLAllColumnExpr());
//        selectQuery.getSelectList().clear();
//        selectQuery.getSelectList().add(count);
//		return statement.toString();
//	}

	@Override
	protected void setAdditionalParameter(BoundSql boundSql) {
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getEndRow()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getBeginRow()));
	}
}
