package org.apache.ibatis.submitted.substitution_in_annots;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SubstitutionInAnnotsMapper {

	public String getPersonNameByIdWithXml(int id);

	@Select("select firstName from ibtest.names where id=${value}")
	public String getPersonNameByIdWithAnnotsValue(int id);

	@Select("select firstName from ibtest.names where id=${_parameter}")
	public String getPersonNameByIdWithAnnotsParameter(int id);

	@Select("select firstName from ibtest.names where id=${named}")
	public String getPersonNameByIdWithAnnotsParamAnnot(@Param("named") int id);

}
