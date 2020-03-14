package org.apache.ibatis.mapping.Dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;

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
	protected void setAdditionalParameter(BoundSql boundSql) {
		boundSql.setAdditionalParameter("autoOffset", new Integer(pageBounds.getBeginRow()));
		boundSql.setAdditionalParameter("autoLimit", new Integer(pageBounds.getEndRow()));
	}

}
