package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.SkillDTO;
import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/skills")
public class SkillsController {
	private final SkillsRepository skillsRepository;
	private final ModelMapper modelMapper;
//	public final String ACCESS_DENIED_ERROR = "Must be authenticated to add a new skill";

	@Autowired
	public SkillsController(SkillsRepository skillsRepository, ModelMapper modelMapper) {
		this.skillsRepository = skillsRepository;
		this.modelMapper = modelMapper;
	}

	@Operation(summary = "Load the list of all skills")
	@GetMapping("/")
	public ResponseEntity<List<SkillDTO>> getAllSkills() {
		List<SkillDTO> skillDTOs = StreamSupport.stream(
				skillsRepository.findAll().spliterator(), false).map(this::convertToDTO).collect(Collectors.toList());
		return ResponseEntity.ok(skillDTOs);
	}

	@Operation(summary = "Create a new skill")
	@PostMapping("/")
	public ResponseEntity<SkillDTO> addSkill(@Valid @RequestBody SkillDTO skillDTO) {
		Skill savedSkill = skillsRepository.save(convertToEntity(skillDTO));
		return ResponseEntity.ok(convertToDTO(savedSkill));
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleValidationExceptions(DataIntegrityViolationException ex) throws Exception{
		return Skill.DUPLICATE_NAME_ERROR;
	}

	private SkillDTO convertToDTO(Skill skill) {
		return SkillDTO.convertToDTO(skill, modelMapper);
	}

	private Skill convertToEntity(SkillDTO skillDTO) {
		return SkillDTO.convertToEntity(skillDTO, modelMapper);
	}
}
