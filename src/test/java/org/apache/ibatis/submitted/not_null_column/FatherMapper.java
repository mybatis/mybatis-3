package org.apache.ibatis.submitted.not_null_column;

public interface FatherMapper
{
	public Father selectByIdNoFid(Integer id);
    public Father selectByIdFid(Integer id);
    public Father selectByIdWithInternalResultMap(Integer id);
    public Father selectByIdWithRefResultMap(Integer id);
    public Father selectByIdFidMultipleNullColumns(Integer id);
    public Father selectByIdFidMultipleNullColumnsAndBrackets(Integer id);
    public Father selectByIdFidWorkaround(Integer id);
}
