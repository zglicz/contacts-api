package com.zglicz.contactsapi.service;

import com.zglicz.contactsapi.dto.SkillDTO;

import java.util.List;

public interface SkillsService {
	List<SkillDTO> getAllSkills();
	SkillDTO addSkill(SkillDTO skillDTO);
}
