package com.zglicz.contactsapi.misc;

import com.zglicz.contactsapi.controllers.ContactsController;
import com.zglicz.contactsapi.entities.ContactSkill;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqueSkillsConstraintValidator implements ConstraintValidator<UniqueSkillsConstraint, List<ContactSkill>> {
	Logger logger = LoggerFactory.getLogger(ContactsController.class);
	@Override
	public boolean isValid(List<ContactSkill> contactSkills, ConstraintValidatorContext context) {
		// Check each skill has an id
		Set<Long> uniqueSkills = contactSkills.stream().filter(contactSkill -> contactSkill.getSkill() != null).map(contactSkill -> contactSkill.getSkill().getId()).collect(Collectors.toSet());
		logger.info("skills", uniqueSkills);
		return uniqueSkills.size() == contactSkills.size();
	}
}
