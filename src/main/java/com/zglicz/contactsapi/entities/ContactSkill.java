package com.zglicz.contactsapi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zglicz.contactsapi.misc.SkillLevel;
import jakarta.persistence.*;

@Entity
public class ContactSkill {
	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private SkillLevel skillLevel;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne
	@JoinColumn(name= "contact_id", nullable = false)
	private Contact contact;

	@ManyToOne
	@JoinColumn(name = "skill_id", nullable = false)
	private Skill skill;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SkillLevel getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(SkillLevel skillLevel) {
		this.skillLevel = skillLevel;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public ContactSkill() {
	}

	public ContactSkill(SkillLevel skillLevel, Skill skill) {
		this.skillLevel = skillLevel;
		this.skill = skill;
	}

	public ContactSkill(SkillLevel skillLevel, Skill skill, Contact contact) {
		this.skillLevel = skillLevel;
		this.skill = skill;
		this.contact = contact;
	}
}
