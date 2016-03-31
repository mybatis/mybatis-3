package org.apache.ibatis.session;

import java.util.List;

/**
 * 
 * @author wenlong.liu
 *
 * @param <E>
 */
public class PageResult<E> implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2614835352594733768L;
	  
	private int page = 1;
	
	private int pageSize = 20;
	
	private int total = 0;
	
	private List<E> queryList = null;
	
	public PageResult(){
		
	}
	
	public PageResult(List<E> queryList, int page, int pagesize, int count)
	{
		this.page = page;
		this.pageSize = pagesize;
		this.queryList = queryList;
		this.total = count;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if(page<1){
			this.page= 1;
		}else{
			this.page = page;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<E> getQueryList() {
		return queryList;
	}

	public void setQueryList(List<E> queryList) {
		this.queryList = queryList;
	}
	
	public int getPageCount() {
		return Math.round((float)this.total/(float)this.pageSize);
	}
}
