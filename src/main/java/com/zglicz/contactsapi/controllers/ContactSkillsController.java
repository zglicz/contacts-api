package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.ContactSkillDTO;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.service.ContactSkillsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@Tag(name = "Contact Skills", description = "Management of skills for contacts")
public class ContactSkillsController {
	public final static String SKILLS_UPDATED_SUCCESS = "Skills saved successfully";
	public final static String SKILL_DELETE_SUCCESS = "Contact skill was deleted";

	private final ContactSkillsService contactSkillsService;
	private final ContactsRepository contactsRepository;

	private final ContactSkillsRepository contactSkillsRepository;
	private final SkillsRepository skillsRepository;
	private final ModelMapper modelMapper;

	public ContactSkillsController(ContactSkillsService contactSkillsService, ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository, SkillsRepository skillsRepository, ModelMapper modelMapper) {
		this.contactSkillsService = contactSkillsService;
		this.contactsRepository = contactsRepository;
		this.contactSkillsRepository = contactSkillsRepository;
		this.skillsRepository = skillsRepository;
		this.modelMapper = modelMapper;
	}

	@Operation(summary = "Get a list of all skills for a contact id")
	@GetMapping("/{id}/skills")
	public ResponseEntity<List<ContactSkillDTO>> getContactSkills(@PathVariable final Long id) {
		return new ResponseEntity<>(contactSkillsService.getContactSkills(id), HttpStatus.OK);
	}

	@Operation(summary = "Update the list of skills for a contact id")
	@PutMapping("/{id}/skills")
	public ResponseEntity<String> updateContactSkills(
			@PathVariable final Long id, @Valid @RequestBody List<ContactSkillDTO> contactSkillDTOs) {
		contactSkillsService.updateContactSkills(id, contactSkillDTOs);
		return ResponseEntity.ok(SKILLS_UPDATED_SUCCESS);
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#contactId)")
	@Operation(summary = "Delete an existing skill for a contact")
	@DeleteMapping("/{contactId}/skills/{contactSkillId}")
	ResponseEntity<String> deleteContactSkill(@PathVariable final Long contactId, @PathVariable final Long contactSkillId) {
		contactSkillsService.deleteContactSkill(contactId, contactSkillId);
		return ResponseEntity.ok(SKILL_DELETE_SUCCESS);
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#contactId)")
	@Operation(summary = "Add a new contact skill, if the skill doesn't yet exist for this contact")
	@PostMapping("/{contactId}/skills")
	ResponseEntity<ContactSkillDTO> addContactSkill(@PathVariable final Long contactId, @RequestBody ContactSkillDTO contactSkillDto) {
		return ResponseEntity.ok(contactSkillsService.addContactSkill(contactId, contactSkillDto));
	}
}
