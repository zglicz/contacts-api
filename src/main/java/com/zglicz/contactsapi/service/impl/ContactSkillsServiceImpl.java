package com.zglicz.contactsapi.service.impl;

import com.zglicz.contactsapi.dto.ContactSkillDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.exceptions.ResourceNotFoundException;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.service.ContactSkillsService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactSkillsServiceImpl implements ContactSkillsService {

	private final ContactsRepository contactsRepository;

	private final ContactSkillsRepository contactSkillsRepository;
	private final SkillsRepository skillsRepository;
	private final ModelMapper modelMapper;

	public ContactSkillsServiceImpl(ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository, SkillsRepository skillsRepository, ModelMapper modelMapper) {
		this.contactsRepository = contactsRepository;
		this.contactSkillsRepository = contactSkillsRepository;
		this.skillsRepository = skillsRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public List<ContactSkillDTO> getContactSkills(Long id) {
		Contact contact = contactsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", id));
		return contactSkillsRepository.findByContactId(contact.getId()).stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#id)")
	@Override
	public void updateContactSkills(Long id, List<ContactSkillDTO> contactSkillDTOs) {
		Contact contact = contactsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", id));
		contactSkillsRepository.deleteAll(contactSkillsRepository.findByContactId(id));
		List<ContactSkill> contactSkills = contactSkillDTOs.stream().map(this::convertToEntity).collect(Collectors.toList());
		contactSkills.stream().forEach(skillContact -> skillContact.setContact(contact));
		contactSkillsRepository.saveAll(contactSkills);
	}

	@Override
	public void deleteContactSkill(Long contactId, Long contactSkillId) {
		contactsRepository.findById(contactId).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));
		ContactSkill contactSkill = contactSkillsRepository.findById(contactSkillId).orElseThrow(() -> new ResourceNotFoundException("ContactSkill", "id", contactSkillId));
		contactSkillsRepository.delete(contactSkill);
	}

	@Override
	public ContactSkillDTO addContactSkill(Long contactId, ContactSkillDTO contactSkillDTO) {
		Contact contact = contactsRepository.findById(contactId).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));
		Skill skill = skillsRepository.findById(contactSkillDTO.getSkillId()).orElseThrow(() -> new ResourceNotFoundException("Skill", "id", contactSkillDTO.getSkillId()));
		Optional<ContactSkill> existingContactSkill = contactSkillsRepository.findByContactIdAndSkillId(contactId, contactSkillDTO.getSkillId());
		if (existingContactSkill.isPresent()) {
			// Potentially think about updating the existing ContactSkill

		}
		ContactSkill newContactSkill = contactSkillsRepository.save(new ContactSkill(contactSkillDTO.getSkillLevel(), skill, contact));
		return convertToDto(newContactSkill);
	}

	private ContactSkillDTO convertToDto(ContactSkill contactSkill) {
		return ContactSkillDTO.convertToDto(contactSkill, modelMapper);
	}

	private ContactSkill convertToEntity(ContactSkillDTO contactSkillDTO) {
		return ContactSkillDTO.convertToEntity(contactSkillDTO, modelMapper);
	}
}
