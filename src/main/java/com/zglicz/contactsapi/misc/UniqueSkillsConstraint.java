package com.zglicz.contactsapi.misc;

import com.zglicz.contactsapi.controllers.ContactsController;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = UniqueSkillsConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueSkillsConstraint {
	String message() default ContactsController.DUPLICATE_SKILLS_ERROR;
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
