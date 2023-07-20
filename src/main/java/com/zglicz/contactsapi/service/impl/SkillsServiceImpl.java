package com.zglicz.contactsapi.service.impl;

import com.zglicz.contactsapi.dto.SkillDTO;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import com.zglicz.contactsapi.service.SkillsService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SkillsServiceImpl implements SkillsService {
	private final SkillsRepository skillsRepository;
	private final ModelMapper modelMapper;

	public SkillsServiceImpl(SkillsRepository skillsRepository, ModelMapper modelMapper) {
		this.skillsRepository = skillsRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public List<SkillDTO> getAllSkills() {
		return StreamSupport.stream(
				skillsRepository.findAll().spliterator(), false).map(this::convertToDTO).collect(Collectors.toList());
	}

	@Override
	public SkillDTO addSkill(SkillDTO skillDTO) {
		return convertToDTO(skillsRepository.save(convertToEntity(skillDTO)));
	}

	private SkillDTO convertToDTO(Skill skill) {
		return SkillDTO.convertToDTO(skill, modelMapper);
	}

	private Skill convertToEntity(SkillDTO skillDTO) {
		return SkillDTO.convertToEntity(skillDTO, modelMapper);
	}
}
