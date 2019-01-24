package io.github.pleuvoir.common.validator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 使用{@link ValueCheck}时，判断值是否是指定的可选值之一，若值为空，将使用空字符串判断可选值，若未设置可选值，将通过校验
 * @author abeir
 *
 */
public class ValueCheckValidator implements ConstraintValidator<ValueCheck, String> {

	private String[] values;
	
	@Override
	public void initialize(ValueCheck constraintAnnotation) {
		values = constraintAnnotation.value();
	}
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(ArrayUtils.isEmpty(values)) {
			return true;
		}
		if(value==null) {
			value = StringUtils.EMPTY;
		}
		return ArrayUtils.contains(values, value);
	}

}
