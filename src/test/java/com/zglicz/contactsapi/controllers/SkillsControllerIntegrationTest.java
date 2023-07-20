package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.exceptions.ResponseExceptionHandler;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.utils.TestUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContactsApiApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SkillsControllerIntegrationTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mvc;
	@Autowired
	private SkillsRepository skillsRepository;
	@Autowired
	private ContactsRepository contactsRepository;
	private Contact contact;

	@BeforeAll
	public void init() {
		contactsRepository.deleteAll();
		contact = contactsRepository.save(TestUtils.getValidContact());
	}

	@BeforeEach
	public void resetDb() {
		skillsRepository.deleteAll();
	}

	@Test
	public void testCreateSkill() throws Exception {
		Skill skill = TestUtils.getValidSkill();
		mvc.perform(
				post("/skills/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(skill))
						.with(httpBasic(TestUtils.DEFAULT_EMAIL, TestUtils.DEFAULT_PASSWORD)));
		List<Skill> skills = (List<Skill>) skillsRepository.findAll();
		Assertions.assertEquals(1, skills.size());
		Skill savedSkill = skills.get(0);
		Assertions.assertEquals(skill.getName(), savedSkill.getName());
	}

	@Test
	public void testGetAllSkills() throws Exception {
		String otherSkillName = "Java";
		createAndSaveSkill(TestUtils.DEFAULT_SKILL_NAME);
		createAndSaveSkill(otherSkillName);

		mvc.perform(get("/skills/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].name", is(otherSkillName)))
				.andExpect(jsonPath("$[1].name", is(TestUtils.DEFAULT_SKILL_NAME)));;
	}

	@Test
	public void testUniqueName() throws Exception {
		createAndSaveSkill(TestUtils.DEFAULT_SKILL_NAME);
		Skill duplicateSkill = TestUtils.getValidSkill();
		mvc.perform(post("/skills/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(duplicateSkill)).with(httpBasic(TestUtils.DEFAULT_EMAIL, TestUtils.DEFAULT_PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString(ResponseExceptionHandler.DUPLICATE_NAME_ERROR)));
	}

	private Skill createAndSaveSkill(String name) {
		Skill skill = TestUtils.getValidSkill(name);
		return skillsRepository.save(skill);
	}
}
