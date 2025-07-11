package com.boot1.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MailValidator implements ConstraintValidator<MailConstraint, String> {
    private String domain;

    @Override
    public boolean isValid(String userMail, ConstraintValidatorContext context) {
        if (userMail == null || userMail.isBlank()) return true;
        return userMail.startsWith(this.domain) && userMail.endsWith("@gmail.com");
    }

    @Override
    public void initialize(MailConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.domain = constraintAnnotation.domain();
    }
}
