package com.zglicz.contactsapi.utils;

import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.Skill;

public abstract class TestUtils {
	public static final String DEFAULT_FIRSTNAME = "Bob";
	public static final String DEFAULT_EMAIL = "bob@example.com";
	public static final String DEFAULT_PASSWORD = "password";
	public static final String DEFAULT_SKILL_NAME = "Ruby";

	public static Contact getValidContact() {
		return getValidContact(TestUtils.DEFAULT_EMAIL);
	}

	public static Contact getValidContact(String email) {
		Contact contact = new Contact();
		contact.setFirstname(TestUtils.DEFAULT_FIRSTNAME);
		contact.setLastname("Ross");
		contact.setEmail(email);
		contact.setPassword(TestUtils.DEFAULT_PASSWORD);
		return contact;
	}

	public static Skill getValidSkill(String name) {
		Skill skill = new Skill();
		skill.setName(name);
		return skill;
	}

	public static Skill getValidSkill() {
		return getValidSkill(DEFAULT_SKILL_NAME);
	}
}
