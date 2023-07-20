package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.ContactsApiApplication;
import com.zglicz.contactsapi.dto.ContactSkillDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.misc.ResponseExceptionHandler;
import com.zglicz.contactsapi.misc.SkillLevel;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.utils.TestUtils;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
	@Autowired
	ModelMapper modelMapper;

	private Contact contact;
	private Skill skill1, skill2, skill3;

	@BeforeAll
	public void init() {
		contact = createNewContact(TestUtils.DEFAULT_EMAIL);
		skill1 = createNewSkill("Java");
		skill2 = createNewSkill("C++");
		skill3 = createNewSkill("Javascript");
		contactSkillsRepository.deleteAll();
	}

	@AfterEach
	public void resetDb() {
		contactSkillsRepository.deleteAll();
	}

	@Test
	public void testGetsAllContactSkills() throws Exception {
		// Empty skills expected
		List<ContactSkill> contactSkills = new ArrayList<>() {{
			add(new ContactSkill(SkillLevel.EXPERT, skill1, contact));
			add(new ContactSkill(SkillLevel.INTERMEDIATE, skill2, contact));
		}};
		contactSkills = (List<ContactSkill>) contactSkillsRepository.saveAll(contactSkills);
		mvc.perform(get("/contacts/" + contact.getId().toString() + "/skills"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(2)))
				.andExpect(jsonPath("$[0].id", is(contactSkills.get(0).getId().intValue())))
				.andExpect(jsonPath("$[1].id", is(contactSkills.get(1).getId().intValue())))
				.andReturn();
	}

	@Test
	public void testReplacesSkills() throws Exception {
		contactSkillsRepository.saveAll(
				Arrays.asList(new ContactSkill(SkillLevel.EXPERT, skill1, contact), new ContactSkill(SkillLevel.INTERMEDIATE, skill2, contact)));
		List<ContactSkillDTO> newSkillDTOs =
				Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill3), new ContactSkill(SkillLevel.EXPERT, skill2)).stream().map(contactSkill -> ContactSkillDTO.convertToDto(contactSkill, modelMapper)).collect(Collectors.toList());
		mvc.perform(
				post("/contacts/" + contact.getId().toString() + "/skills")
						.with(httpBasic(TestUtils.DEFAULT_EMAIL, TestUtils.DEFAULT_PASSWORD))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newSkillDTOs)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(ContactSkillsController.SKILLS_UPDATED_SUCCESS)));
		List<ContactSkill> resultSkills = contactSkillsRepository.findByContactId(contact.getId());
		Set<String> expectedSkillNames = newSkillDTOs.stream().map(ContactSkillDTO::getSkillName).collect(Collectors.toSet());
		Set<String> actualSkillNames = resultSkills.stream().map(contactSkill -> contactSkill.getSkill().getName()).collect(Collectors.toSet());
		Assertions.assertEquals(expectedSkillNames, actualSkillNames);
	}

	@Test
	public void testDontAllowChangingOthersContactSkill() throws Exception {
		Contact contact2 = createNewContact("other_email@example.com");
		List<ContactSkill> newSkills =
				Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill3), new ContactSkill(SkillLevel.EXPERT, skill2));
		mvc.perform(
				post("/contacts/" + contact.getId().toString() + "/skills")
						.with(httpBasic(contact2.getUsername(), TestUtils.DEFAULT_PASSWORD))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newSkills)))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(containsString(ResponseExceptionHandler.ACCESS_DENIED_ERROR)));
	}

	@Test
	public void testDontAllowMultipleContactSkillsWithSameSkill() throws Exception {
		List<ContactSkillDTO> contactSkillDTOs =
				Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill1), new ContactSkill(SkillLevel.BEGINNER, skill1))
						.stream()
						.map(contactSkill -> ContactSkillDTO.convertToDto(contactSkill, modelMapper))
						.collect(Collectors.toList());
		MvcResult mvcResult = mvc.perform(
				post("/contacts/" + contact.getId() + "/skills")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(contactSkillDTOs))
						.with(httpBasic(contact.getUsername(), TestUtils.DEFAULT_PASSWORD)))
				.andExpect(status().isBadRequest()).andReturn();
		Assertions.assertEquals(ResponseExceptionHandler.DUPLICATE_SKILLS_ERROR, mvcResult.getResponse().getContentAsString());
	}

	@Test
	public void testHandlesInvalidRequest() throws Exception {
		List<ContactSkill> contactSkills = Arrays.asList(new ContactSkill(SkillLevel.INTERMEDIATE, skill1));
		mvc.perform(
				post("/contacts/" + contact.getId() + "/skills")
						.with(httpBasic(contact.getUsername(), TestUtils.DEFAULT_PASSWORD))
						.contentType(MediaType.APPLICATION_JSON)
						.content("[{\"skillLevel\": \"EXPERT\", \"skill_id\": 1}]"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testCanDeleteContactSkill() throws Exception {
		ContactSkill contactSkill = contactSkillsRepository.save(new ContactSkill(SkillLevel.EXPERT, skill1, contact));
		mvc.perform(
				delete("/contacts/" + contact.getId() + "/skills/" + contactSkill.getId())
						.with(httpBasic(contact.getUsername(), TestUtils.DEFAULT_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(ContactSkillsController.SKILL_DELETE_SUCCESS)));
	}


	private Contact createNewContact(String email) {
		Contact contact = TestUtils.getValidContact(email);
		return contactsRepository.save(contact);
	}

	private Skill createNewSkill(String name) {
		Skill skill = new Skill();
		skill.setName(name);
		return skillsRepository.save(skill);
	}
}
