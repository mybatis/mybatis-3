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
		StringBuffer buffer = new StringBuffer(this.boundSql.getSql()+100);
		buffer.append("select * from (select rownum rn, t.* from (");
		buffer.append(this.boundSql.getSql());
		buffer.append(this.boundSql.getSql());
		buffer.append(")t where ROWNUM <=?)tt where tt.rn >?");
		return buffer.toString();
	}
	
	@Override
	public String bulidCountSql() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("select count(1) from (")
			.append(this.boundSql.getSql())
		.append(")");
		return buffer.toString();
	}

}
