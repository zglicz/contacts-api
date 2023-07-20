package com.zglicz.contactsapi.service;

import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.dto.ContactsResponse;

public interface ContactsService {
	ContactsResponse getContacts(int pageNo, int pageSize);
	ContactDTO getContact(Long id);

	void deleteContact(Long id);

	ContactDTO updateContact(Long id, ContactDTO updatedContact);

	ContactDTO createContact(ContactDTO contactDTO);
}
