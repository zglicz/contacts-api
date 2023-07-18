package com.zglicz.contactsapi.repositories;

import com.zglicz.contactsapi.entities.ContactSkill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactSkillsRepository extends CrudRepository<ContactSkill, Long> {
	List<ContactSkill> findByContactId(Long id);
}
