package com.ktcloud.daangn.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValidator implements ConstraintValidator<EnumValid, String> {

    private Set<String> allowedValues;
    private boolean ignoreCase;
    private String messageTemplate;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        ignoreCase = constraintAnnotation.ignoreCase();
        Enum<?>[] enumConstants = constraintAnnotation.target().getEnumConstants();

        // Enum 상수 이름들을 Set으로 수집 (ignoreCase가 true면 대문자로 변환하여 저장)
        allowedValues = Arrays.stream(enumConstants)
                .map(Enum::name)
                .map(this::checkIgnoreCase)
                .collect(Collectors.toSet());

        messageTemplate = convertMessageTemplate(constraintAnnotation.message());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String convertedValue = checkIgnoreCase(value);

        if (allowedValues.contains(convertedValue)) {
            return true;
        }

        // 기본 제약 조건 위반 메시지 비활성화 및 커스텀 메시지 설정
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation();

        return false;
    }

    private String convertMessageTemplate(String message) {
        return message.replace("{enumValues}", String.join(" | ", allowedValues));
    }

    private String checkIgnoreCase(String value) {
        return this.ignoreCase ? value.toUpperCase() : value;
    }
}