package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.ContactSkillDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contacts")
@Tag(name = "Contact Skills", description = "Management of skills for contacts")
public class ContactSkillsController {
	public final static String SKILLS_UPDATED_SUCCESS = "Skills saved successfully";
	public final static String SKILL_DELETE_SUCCESS = "Contact skill was deleted";

	private final ContactsRepository contactsRepository;

	private final ContactSkillsRepository contactSkillsRepository;
	private final SkillsRepository skillsRepository;
	private final ModelMapper modelMapper;

	public ContactSkillsController(ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository, SkillsRepository skillsRepository, ModelMapper modelMapper) {
		this.contactsRepository = contactsRepository;
		this.contactSkillsRepository = contactSkillsRepository;
		this.skillsRepository = skillsRepository;
		this.modelMapper = modelMapper;
	}

	@Operation(summary = "Get a list of all skills for a contact id")
	@GetMapping("/{id}/skills")
	public ResponseEntity<List<ContactSkillDTO>> getContactSkills(@PathVariable final Long id) {
		return contactsRepository.findById(id)
				.map(contact -> new ResponseEntity<>(contactSkillsRepository.findByContactId(id).stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK))
				.orElse(ResponseEntity.notFound().build());
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#id)")
	@Operation(summary = "Update the list of skills for a contact id")
	@PutMapping("/{id}/skills")
	public ResponseEntity<String> updateContactSkills(
			@PathVariable final Long id, @Valid @RequestBody List<ContactSkillDTO> contactSkillDTOs) {
		return contactsRepository.findById(id)
				.map(contact -> {
					contactSkillsRepository.deleteAll(contactSkillsRepository.findByContactId(id));
					List<ContactSkill> contactSkills = contactSkillDTOs.stream().map(this::convertToEntity).collect(Collectors.toList());
					contactSkills.stream().forEach(skillContact -> skillContact.setContact(contact));
					contactSkillsRepository.saveAll(contactSkills);
					return ResponseEntity.ok(SKILLS_UPDATED_SUCCESS);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#contactId)")
	@Operation(summary = "Delete an existing skill for a contact")
	@DeleteMapping("/{contactId}/skills/{contactSkillId}")
	ResponseEntity<String> deleteContactSkill(@PathVariable final Long contactId, @PathVariable final Long contactSkillId) {
		return contactsRepository.findById(contactId)
				.map(contact -> {
					return contactSkillsRepository.findById(contactSkillId).map(contactSkill -> {
						contactSkillsRepository.delete(contactSkill);
						return ResponseEntity.ok(SKILL_DELETE_SUCCESS);
					}).orElse(ResponseEntity.notFound().build());
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#contactId)")
	@Operation(summary = "Add a new contact skill, if the skill doesn't yet exist for this contact")
	@PostMapping("/{contactId}/skills")
	ResponseEntity<ContactSkillDTO> addContactSkill(@PathVariable final Long contactId, @RequestBody ContactSkillDTO contactSkillDto) {
		Optional<Contact> contact = contactsRepository.findById(contactId);
		if (!contact.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Optional<Skill> skill = skillsRepository.findById(contactSkillDto.getSkillId());
		if (!skill.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		Optional<ContactSkill> existingContactSkill = contactSkillsRepository.findByContactIdAndSkillId(contact.get().getId(), skill.get().getId());
		if (existingContactSkill.isPresent()) {
			// Potentially think about updating the existing ContactSkill
			return ResponseEntity.badRequest().build();
		}
		ContactSkill newContactSkill = contactSkillsRepository.save(new ContactSkill(contactSkillDto.getSkillLevel(), skill.get(), contact.get()));
		return ResponseEntity.ok(convertToDto(newContactSkill));
	}

	private ContactSkillDTO convertToDto(ContactSkill contactSkill) {
		return ContactSkillDTO.convertToDto(contactSkill, modelMapper);
	}

	private ContactSkill convertToEntity(ContactSkillDTO contactSkillDTO) {
		return ContactSkillDTO.convertToEntity(contactSkillDTO, modelMapper);
	}
}
