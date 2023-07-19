package com.zglicz.contactsapi.repositories;

import com.zglicz.contactsapi.entities.Contact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactsRepository extends CrudRepository<Contact, Long> {
	Optional<Contact> findByEmail(String username);
}
