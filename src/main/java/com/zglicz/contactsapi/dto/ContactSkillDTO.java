package com.zglicz.contactsapi.dto;

import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.misc.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ContactSkillDTO {
	private Long id;
	private SkillLevel skillLevel;
	private Long skillId;
	private String skillName;

	public static ContactSkillDTO convertToDto(ContactSkill contactSkill, ModelMapper modelMapper) {
		ContactSkillDTO contactSkillDTO = modelMapper.map(contactSkill, ContactSkillDTO.class);
		return contactSkillDTO;
	}

	public static ContactSkill convertToEntity(ContactSkillDTO contactSkillDTO, ModelMapper modelMapper) {
		ContactSkill contactSkill = modelMapper.map(contactSkillDTO, ContactSkill.class);
		return contactSkill;
	}
}
