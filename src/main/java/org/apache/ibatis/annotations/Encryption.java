package org.apache.ibatis.annotations;



import org.apache.ibatis.plugin.encryption.CryptogramType;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Encryption {
	
	CryptogramType type() default CryptogramType.AES256;
	
	
	
}
