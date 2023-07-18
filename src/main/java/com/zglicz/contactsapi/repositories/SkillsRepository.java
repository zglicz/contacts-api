package com.zglicz.contactsapi.repositories;

import com.zglicz.contactsapi.entities.Skill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillsRepository extends CrudRepository<Skill, Long> {
}
