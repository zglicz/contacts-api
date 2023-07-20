package com.zglicz.contactsapi.repositories;

import com.zglicz.contactsapi.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactsRepository extends JpaRepository<Contact, Long> {
	Optional<Contact> findByEmail(String username);
}
