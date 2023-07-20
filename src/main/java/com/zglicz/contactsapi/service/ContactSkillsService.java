package com.zglicz.contactsapi.service;

import com.zglicz.contactsapi.dto.ContactSkillDTO;

import java.util.List;

public interface ContactSkillsService {
	List<ContactSkillDTO> getContactSkills(Long id);
	void updateContactSkills(Long id, List<ContactSkillDTO> contactSkillDTOs);
	void deleteContactSkill(Long contactId, Long contactSkillId);
	ContactSkillDTO addContactSkill(Long contactId, ContactSkillDTO contactSkillDTO);
}
