package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.SkillDTO;
import com.zglicz.contactsapi.service.SkillsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@Tag(name = "Skills", description = "Management of global skills")
public class SkillsController {
	private final SkillsService skillsService;

	@Autowired
	public SkillsController(SkillsService skillsService) {
		this.skillsService = skillsService;
	}

	@Operation(summary = "Load the list of all skills")
	@GetMapping("/")
	public ResponseEntity<List<SkillDTO>> getAllSkills() {
		return ResponseEntity.ok(skillsService.getAllSkills());
	}

	@Operation(summary = "Create a new skill")
	@PostMapping("/")
	public ResponseEntity<SkillDTO> addSkill(@Valid @RequestBody SkillDTO skillDTO) {
		return ResponseEntity.ok(skillsService.addSkill(skillDTO));
	}
}
