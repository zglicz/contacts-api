package com.zglicz.contactsapi.utils;

import com.zglicz.contactsapi.entities.Contact;

public abstract class TestUtils {
	public static final String DEFAULT_FIRSTNAME = "Bob";
	public static final String DEFAULT_EMAIL = "bob@example.com";
	public static final String DEFAULT_PASSWORD = "password";

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
}
