package org.apache.ibatis.submitted.awful_table;

public interface AwfulTableMapper {

    int deleteByPrimaryKey(Integer customerId);

    int insert(AwfulTable record);

    int insertSelective(AwfulTable record);

    AwfulTable selectByPrimaryKey(Integer customerId);

    int updateByPrimaryKeySelective(AwfulTable record);

    int updateByPrimaryKey(AwfulTable record);
}