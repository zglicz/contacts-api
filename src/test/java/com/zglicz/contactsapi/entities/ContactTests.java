package com.zglicz.contactsapi.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContactTests {

    @Autowired
    private Validator validator;

    @Test
    void testValidInput() {
        Contact contact = new Contact();
        contact.setFirstname("Bob");
        contact.setLastname("Ross");
        contact.setEmail("example@email.com");

        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        assertTrue(violations.isEmpty());
    }
    @Test
    void testInvalidEmail() {
        Contact contact = new Contact();
        contact.setFirstname("Bob");
        contact.setLastname("Ross");
        contact.setEmail("bad-email");

        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        assertFalse(violations.isEmpty());
        assertEquals("Invalid email address", violations.iterator().next().getMessage());
    }
}
