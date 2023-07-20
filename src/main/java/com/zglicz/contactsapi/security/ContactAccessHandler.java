package com.zglicz.contactsapi.security;

import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.service.AuthenticatedUserService;
import org.springframework.stereotype.Component;

@Component("contactAccess")
public class ContactAccessHandler {
	private final ContactsRepository contactsRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public ContactAccessHandler(ContactsRepository contactsRepository, AuthenticatedUserService authenticatedUserService) {
		this.contactsRepository = contactsRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	public boolean canUpdateContact(Long id) {
		Contact contact = authenticatedUserService.getAuthenticatedUser();
		return id == contact.getId();
	}
}
