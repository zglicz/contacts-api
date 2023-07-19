package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.entities.Skill;
import com.zglicz.contactsapi.repositories.SkillsRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
public class SkillsController {
	private final SkillsRepository skillsRepository;

	@Autowired
	public SkillsController(SkillsRepository skillsRepository) {
		this.skillsRepository = skillsRepository;
	}

	@Operation(summary = "Load the list of all skills")
	@GetMapping("/")
	public ResponseEntity<List<Skill>> getAllSkills() {
		return ResponseEntity.ok((List<Skill>) skillsRepository.findAll());
	}

	@Operation(summary = "Create a new skill")
	@PostMapping("/")
	public ResponseEntity<Skill> addSkill(@Valid @RequestBody Skill skill) {
		Skill savedSkill = skillsRepository.save(skill);
		return ResponseEntity.ok(savedSkill);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleValidationExceptions(DataIntegrityViolationException ex) throws Exception{
		return Skill.DUPLICATE_NAME_ERROR;
	}
}
