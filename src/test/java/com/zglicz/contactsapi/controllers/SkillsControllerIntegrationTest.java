package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContactsApiApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class SkillsControllerIntegrationTest {
	public static final String DEFAULT_SKILL_NAME = "C++";

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mvc;
	@Autowired
	private SkillsRepository skillsRepository;

	@AfterEach
	public void resetDb() { skillsRepository.deleteAll(); }

	@Test
	public void testCreateSkill() throws Exception {
		Skill skill = getValidSkill();
		mvc.perform(post("/skills/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(skill)));
		List<Skill> skills = (List<Skill>) skillsRepository.findAll();
		Assertions.assertTrue(skills.size() == 1);
		Skill savedSkill = skills.get(0);
		Assertions.assertEquals(skill.getName(), savedSkill.getName());
	}

	@Test
	public void testGetAllSkills() throws Exception {
		String otherSkillName = "Java";
		createAndSaveSkill(DEFAULT_SKILL_NAME);
		createAndSaveSkill(otherSkillName);

		mvc.perform(get("/skills/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].name", is(DEFAULT_SKILL_NAME)))
				.andExpect(jsonPath("$[1].name", is(otherSkillName)));;
	}

	@Test
	public void testUniqueName() throws Exception {
		createAndSaveSkill(DEFAULT_SKILL_NAME);
		Skill duplicateSkill = getValidSkill();
		mvc.perform(post("/skills/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(duplicateSkill)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString(Skill.DUPLICATE_NAME_ERROR)));
	}

	private Skill getValidSkill(String name) {
		Skill skill = new Skill();
		skill.setName(name);
		return skill;
	}

	private Skill getValidSkill() {
		return getValidSkill(DEFAULT_SKILL_NAME);
	}

	private Skill createAndSaveSkill(String name) {
		Skill skill = getValidSkill(name);
		return skillsRepository.save(skill);
	}
}
