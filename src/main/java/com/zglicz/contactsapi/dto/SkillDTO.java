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
		return modelMapper.map(skill, SkillDTO.class);
	}

	public static Skill convertToEntity(SkillDTO skillDTO, ModelMapper modelMapper) {
		return modelMapper.map(skillDTO, Skill.class);
	}
}
