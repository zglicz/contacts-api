package com.zglicz.contactsapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Skill {
	public final static String NAME_REQUIRED_ERROR = "Name is required";

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank(message = NAME_REQUIRED_ERROR)
	@Size(min = 2, max = 50)
	@Column(unique = true)
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
