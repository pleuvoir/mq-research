package io.github.pleuvoir.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 标注属性值必须是指定的可选值之一，若值为空，将使用空字符串判断可选值，若未设置可选值，将通过校验
 * @author abeir
 *
 */
@Constraint(validatedBy = ValueCheckValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)  
@Documented 
public @interface ValueCheck {

	String[] value() default {};
	
	String message() default "";
	
	//下面这两个属性必须添加  
    Class<?>[] groups() default {};  
    
    Class<? extends Payload>[] payload() default {};  
}
