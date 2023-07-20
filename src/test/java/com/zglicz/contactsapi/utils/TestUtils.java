package com.zglicz.contactsapi.utils;

import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.Skill;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestUtils {
	public static final String DEFAULT_FIRSTNAME = "Bob";
	public static final String DEFAULT_LASTNAME = "Ross";
	public static final String DEFAULT_EMAIL = "bob@example.com";
	public static final String DEFAULT_PASSWORD = "password";
	public static final String DEFAULT_SKILL_NAME = "Ruby";


	public static Contact getValidContact() {
		return getValidContact(TestUtils.DEFAULT_EMAIL);
	}

	public static ContactDTO getValidContactDTO() {
		return new ContactDTO(null, DEFAULT_FIRSTNAME, DEFAULT_LASTNAME, DEFAULT_EMAIL, null, null, DEFAULT_PASSWORD);
	}

	public static Contact getValidContact(String email) {
		Contact contact = new Contact();
		contact.setFirstname(DEFAULT_FIRSTNAME);
		contact.setLastname("Ross");
		contact.setEmail(email);
		contact.setPassword(getEncryptedPassword());
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

	public static String getEncryptedPassword() {
		return getEncryptedPassword(DEFAULT_PASSWORD);
	}

	public static String getEncryptedPassword(String password) {
		return (new BCryptPasswordEncoder()).encode(password);
	}
}
