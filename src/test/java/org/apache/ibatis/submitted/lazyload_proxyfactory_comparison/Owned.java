package org.apache.ibatis.submitted.lazyload_proxyfactory_comparison;

public interface Owned<OWNERTYPE> {
	OWNERTYPE getOwner();
	void setOwner(OWNERTYPE owner);

}
