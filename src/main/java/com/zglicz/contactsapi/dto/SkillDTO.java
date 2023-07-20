package com.zglicz.contactsapi.dto;

import com.zglicz.contactsapi.entities.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SkillDTO {
	private Long id;
	private String name;

	public static SkillDTO convertToDTO(Skill skill, ModelMapper modelMapper) {
		SkillDTO skillDTO = modelMapper.map(skill, SkillDTO.class);
		return skillDTO;
	}

	public static Skill convertToEntity(SkillDTO skillDTO, ModelMapper modelMapper) {
		Skill skill = modelMapper.map(skillDTO, Skill.class);
		return skill;
	}
}
