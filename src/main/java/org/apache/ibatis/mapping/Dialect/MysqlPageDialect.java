package org.apache.ibatis.mapping.Dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;


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
		StringBuffer buffer = new StringBuffer();
		buffer.append("select count(1) from (")
			.append(this.boundSql.getSql())
		.append(")");
		return buffer.toString();
	}

}
