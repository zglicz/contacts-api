package com.zglicz.contactsapi.repositories;

import com.zglicz.contactsapi.entities.Contact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactsRepository extends CrudRepository<Contact, Long> {
}
