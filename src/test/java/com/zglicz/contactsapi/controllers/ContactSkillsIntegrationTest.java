package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.misc.SkillLevel;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContactsApiApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContactSkillsIntegrationTest {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mvc;

	@Autowired
	ContactSkillsRepository contactSkillsRepository;
	@Autowired
	ContactsRepository contactsRepository;
	@Autowired
	SkillsRepository skillsRepository;

	private Contact contact;
	private Skill skill1, skill2, skill3;

	@BeforeAll
	public void init() {
		contact = createNewContact();
		skill1 = createNewSkill("Java");
		skill2 = createNewSkill("C++");
		skill3 = createNewSkill("Javascript");
	}

	@AfterEach
	public void resetDb() {
		contactSkillsRepository.deleteAll();
	}

	@Test
	public void testSavesContactSkills() throws Exception {
		// Empty skills expected
		mvc.perform(get("/contacts/" + contact.getId().toString() + "/skills"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(0)));

		List<ContactSkill> contactSkills = new ArrayList<>() {{
			add(new ContactSkill(SkillLevel.EXPERT, skill1, contact));
			add(new ContactSkill(SkillLevel.INTERMEDIATE, skill2, contact));
		}};
		contactSkillsRepository.saveAll(contactSkills);
		mvc.perform(get("/contacts/" + contact.getId().toString() + "/skills"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].id", is(skill1.getId().intValue())))
				.andExpect(jsonPath("$[1].id", is(skill2.getId().intValue())));
	}

	@Test
	public void testReplacesSkills() throws Exception {
		contactSkillsRepository.saveAll(
				Arrays.asList(new ContactSkill(SkillLevel.EXPERT, skill1, contact), new ContactSkill(SkillLevel.INTERMEDIATE, skill2, contact)));
		List<ContactSkill> newSkills =
				Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill3), new ContactSkill(SkillLevel.EXPERT, skill2));
		mvc.perform(post("/contacts/" + contact.getId().toString() + "/skills").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(newSkills)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(ContactsController.SKILLS_UPDATED_SUCCESS)));
		List<ContactSkill> resultSkills = contactSkillsRepository.findByContactId(contact.getId());
		Set<String> expectedSkillNames = newSkills.stream().map(contactSkill -> contactSkill.getSkill().getName()).collect(Collectors.toSet());
		Set<String> actualSkillNames = resultSkills.stream().map(contactSkill -> contactSkill.getSkill().getName()).collect(Collectors.toSet());
		Assertions.assertEquals(expectedSkillNames, actualSkillNames);
	}

	@Test
	public void testDontAllowMultipleContactSkillsWithSameSkill() throws Exception {
		List<ContactSkill> contactSkills = Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill1), new ContactSkill(SkillLevel.BEGINNER, skill1));
		MvcResult mvcResult = mvc.perform(post("/contacts/" + contact.getId() + "/skills").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(contactSkills)))
				.andExpect(status().isBadRequest())
				.andReturn();
		Assertions.assertEquals(ContactsController.DUPLICATE_SKILLS_ERROR, mvcResult.getResponse().getContentAsString());
	}

	private Contact createNewContact() {
		Contact contact = new Contact();
		contact.setFirstname("Bob");
		contact.setLastname("Ross");
		contact.setEmail("bob@example.com");
		return contactsRepository.save(contact);
	}

	private Skill createNewSkill(String name) {
		Skill skill = new Skill();
		skill.setName(name);
		return skillsRepository.save(skill);
	}
}
