package com.zglicz.contactsapi.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.utils.TestUtils;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContactsApiApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class ContactsControllerIntegrationTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mvc;
	@Autowired
	private ContactsRepository contactsRepository;
	@Autowired
	private SkillsRepository skillsRepository;

	@AfterEach
	public void resetDb() {
		contactsRepository.deleteAll();
		skillsRepository.deleteAll();
	}

	@Test
	public void testCreateContact() throws Exception {
		Contact contact = TestUtils.getValidContact();
		mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact)));
		List<Contact> contacts = (List<Contact>) contactsRepository.findAll();
		Assertions.assertEquals(1, contacts.size());
		Contact savedContact = contacts.get(0);
		Assertions.assertEquals(contact.getFirstname(), savedContact.getFirstname());
		Assertions.assertEquals(contact.getEmail(), savedContact.getEmail());
	}

	@Test
	public void testGetAllContacts() throws Exception {
		String otherEmail = "otherBob@example.com";
		Contact contact1 = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		Contact contact2 = createAndSaveContact(otherEmail);
		mvc.perform(get("/contacts/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].email", is(TestUtils.DEFAULT_EMAIL)))
				.andExpect(jsonPath("$[0].id", is(contact1.getId().intValue())))
				.andExpect(jsonPath("$[1].email", is(otherEmail)))
				.andExpect(jsonPath("$[1].id", is(contact2.getId().intValue())));
	}

	@Test
	public void testGetSingleContact() throws Exception {
		Contact contact = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		mvc.perform(get("/contacts/" + contact.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$['firstname']", is(TestUtils.DEFAULT_FIRSTNAME)))
				.andExpect(jsonPath("$['id']", is(contact.getId().intValue())));
	}

	@Test
	public void testUpdateUser() throws Exception {
		Contact contact = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		String updatedFirstname = "Bobby";
		contact.setFirstname(updatedFirstname);
		mvc.perform(
					put("/contacts/" + contact.getId().toString())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(contact))
							.with(httpBasic(TestUtils.DEFAULT_EMAIL, TestUtils.DEFAULT_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$['firstname']", is(updatedFirstname)))
				.andExpect(jsonPath("$['id']", is(contact.getId().intValue())));
	}

	@Test
	public void testDeleteUser() throws Exception {
		Contact contact = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		mvc.perform(delete("/contacts/" + contact.getId().toString()).with(httpBasic(TestUtils.DEFAULT_EMAIL, TestUtils.DEFAULT_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(ContactsController.CONTACT_DELETED_SUCCESS)));
		Assertions.assertEquals(0, contactsRepository.count());
	}

	@Test
	public void testCannotDeleteUserUnauthenticated() throws Exception {
		Contact contact = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		mvc.perform(delete("/contacts/" + contact.getId().toString()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testInvalidFieldsErrors() throws Exception {
		Contact contact = new Contact();
		contact.setEmail("invalid.email");
		contact.setFirstname("A");
		contact.setLastname(new String(new char[60]).replace('\0', ' '));

		MvcResult mvcResult = mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact)))
				.andExpect(status().isBadRequest())
				.andReturn();
		Map<String, String> expectedErrors = new HashMap<>() {{
			put("firstname", Contact.FIRSTNAME_LENGTH_ERROR);
			put("password", Contact.PASSWORD_NOT_EMPTY_ERROR);
			put("email", Contact.EMAIL_INVALID_ERROR);
			put("lastname", Contact.LASTNAME_LENGTH_ERROR);
		}};
		@SuppressWarnings("unchecked")
		Map<String, String> actualErrors = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), HashMap.class);
		Assertions.assertEquals(expectedErrors, actualErrors);
	}

	@Test
	public void testUniqueEmailConstraint() throws Exception {
		createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		Contact contact2 = TestUtils.getValidContact(TestUtils.DEFAULT_EMAIL);
		mvc.perform(post("/contacts/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contact2)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString(Contact.EMAIL_DUPLICATE_ERROR)));
	}

	@Test
	public void testCannotUpdateOtherUser() throws Exception {
		Contact contact1 = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		Contact contact2 = createAndSaveContact("other_email@example.com");

		// contact2 tries to update contact1
		mvc.perform(
				put("/contacts/" + contact1.getId().toString())
						.with(httpBasic(contact2.getUsername(), contact2.getPassword()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(contact1)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(containsString(ContactsController.ACCESS_DENIED_ERROR)));
	}

	@Test
	public void testCannotDeleteOtherUser() throws Exception {
		Contact contact1 = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		Contact contact2 = createAndSaveContact("other_email@example.com");

		// contact2 tries to update contact1
		mvc.perform(
						delete("/contacts/" + contact1.getId().toString())
								.with(httpBasic(contact2.getUsername(), contact2.getPassword())))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(containsString(ContactsController.ACCESS_DENIED_ERROR)));
	}

	@Test
	public void testCannotUpdateOtherUsersSkills() throws Exception {
		Contact contact1 = createAndSaveContact(TestUtils.DEFAULT_EMAIL);
		Contact contact2 = createAndSaveContact("other_email@example.com");
		Skill skill = createAndSaveSkill();
		mvc.perform(
						post("/contacts/" + contact1.getId().toString() + "/skills")
								.with(httpBasic(contact2.getUsername(), contact2.getPassword()))
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(List.of(skill))))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(containsString(ContactsController.ACCESS_DENIED_ERROR)));
	}

	private Contact createAndSaveContact(String email) {
		Contact contact = TestUtils.getValidContact(email);
		return contactsRepository.save(contact);
	}

	private Skill createAndSaveSkill() {
		Skill skill = TestUtils.getValidSkill();
		return skillsRepository.save(skill);
	}
}
