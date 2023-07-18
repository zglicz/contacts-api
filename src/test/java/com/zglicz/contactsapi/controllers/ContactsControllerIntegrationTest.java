package com.zglicz.contactsapi.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContactsApiApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class ContactsControllerIntegrationTest {
	public static final String DEFAULT_FIRSTNAME = "Bob";
	public static final String DEFAULT_EMAIL = "bob@example.com";

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mvc;
	@Autowired
	private ContactsRepository contactsRepository;

	@AfterEach
	public void resetDb() {
		contactsRepository.deleteAll();
	}

	@Test
	public void testCreateContact() throws Exception {
		Contact contact = getValidContact();
		mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact)));
		List<Contact> contacts = (List<Contact>) contactsRepository.findAll();
		Assertions.assertTrue(contacts.size() == 1);
		Contact savedContact = contacts.get(0);
		Assertions.assertEquals(contact.getFirstname(), savedContact.getFirstname());
		Assertions.assertEquals(contact.getEmail(), savedContact.getEmail());
	}

	@Test
	public void testGetAllContacts() throws Exception {
		String otherEmail = "otherBob@example.com";
		Contact contact1 = createAndSaveContact(DEFAULT_EMAIL);
		Contact contact2 = createAndSaveContact(otherEmail);
		List<Contact> contacts = (List<Contact>) contactsRepository.findAll();
		List<String> expectedNames = contacts.stream().map(Contact::getFirstname).collect(Collectors.toList());
		mvc.perform(get("/contacts/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].email", is(DEFAULT_EMAIL)))
				.andExpect(jsonPath("$[1].email", is(otherEmail)));
	}

	@Test
	public void testGetSingleContact() throws Exception {
		Contact contact = createAndSaveContact(DEFAULT_EMAIL);
		mvc.perform(get("/contacts/" + contact.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$['firstname']", is(DEFAULT_FIRSTNAME)))
				.andExpect(jsonPath("$['id']", is(contact.getId().intValue())));
	}

	@Test
	public void testUpdateUser() throws Exception {
		Contact contact = createAndSaveContact(DEFAULT_EMAIL);
		String updatedFirstname = "Bobby";
		contact.setFirstname(updatedFirstname);
		mvc.perform(put("/contacts/" + contact.getId().toString()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact)))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$['firstname']", is(updatedFirstname)))
				.andExpect(jsonPath("$['id']", is(contact.getId().intValue())));
	}

	@Test
	public void testDeleteUser() throws Exception {
		Contact contact = createAndSaveContact(DEFAULT_EMAIL);
		mvc.perform(delete("/contacts/" + contact.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(ContactsController.CONTACT_DELETED_SUCCESS)));
		Assertions.assertEquals(0, contactsRepository.count());
	}

	@Test
	public void testInvalidFieldsErrors() throws Exception {
		Contact contact = new Contact();
		contact.setEmail("invalid.email");

		MvcResult mvcResult = mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact)))
				.andExpect(status().isBadRequest())
				.andReturn();
		Map<String, String> expectedErrors = new HashMap<>() {{
			put("firstname", Contact.FIRSTNAME_REQUIRED_ERROR);
			put("lastname", Contact.LASTNAME_REQUIRED_ERROR);
			put("email", Contact.EMAIL_INVALID_ERROR);
		}};
		Assertions.assertEquals(
				objectMapper.writeValueAsString(expectedErrors), mvcResult.getResponse().getContentAsString());
	}

	@Test
	public void testUniqueEmailConstraint() throws Exception {
		Contact contact1 = createAndSaveContact(DEFAULT_EMAIL);
		Contact contact2 = getValidContact(DEFAULT_EMAIL);
		mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact2)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString(Contact.EMAIL_DUPLICATE_ERROR)));
	}

	private Contact createAndSaveContact(String email) {
		Contact contact = getValidContact(email);
		return contactsRepository.save(contact);
	}

	private Contact getValidContact() {
		return getValidContact(DEFAULT_EMAIL);
	}

	private Contact getValidContact(String email) {
		Contact contact = new Contact();
		contact.setFirstname(DEFAULT_FIRSTNAME);
		contact.setLastname("Ross");
		contact.setEmail(email);
		return contact;
	}
}
