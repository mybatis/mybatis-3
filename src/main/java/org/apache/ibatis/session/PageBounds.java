/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session;

/**
 * @author wenlong.liu
 */
public class PageBounds {

	//current query page
	private int page = 1;
	//evey page size
	private int pageSize = 20;
	//row count
	private int count = 0;

  	public PageBounds(int page, int pageSize) {
  		if(page<1) page = 1;
	    this.page = page;
	    this.pageSize = pageSize;
  	}

	public int getPage() {
		return page;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getOffset() {
		return (page-1) * pageSize;
	}

	public int getLimit() {
		return pageSize;
    }
	
	public int getBeginRow() {
		return (page-1) * pageSize;
	}
	
	public int getEndRow() {
		return page * pageSize;
	}
	
	public RowBounds getRowBounds(){
		return new RowBounds(getOffset(), getLimit());
	}
}
