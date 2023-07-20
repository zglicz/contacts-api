package com.zglicz.contactsapi.service.impl;

import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.dto.ContactsResponse;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.exceptions.ResourceNotFoundException;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import com.zglicz.contactsapi.service.ContactsService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactsServiceImpl implements ContactsService {
	private final ContactsRepository contactsRepository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;

	public ContactsServiceImpl(ContactsRepository contactsRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
		this.contactsRepository = contactsRepository;
		this.passwordEncoder = passwordEncoder;
		this.modelMapper = modelMapper;
	}

	@Override
	public ContactsResponse getContacts(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Contact> contacts = contactsRepository.findAll(pageable);
		ContactsResponse contactsResponse = new ContactsResponse();
		List<ContactDTO> contactDTOs = contacts.getContent().stream().map(this::convertToDto).collect(Collectors.toList());
		contactsResponse.setContacts(contactDTOs);
		contactsResponse.setPageNo(contacts.getNumber());
		contactsResponse.setPageSize(contacts.getSize());
		contactsResponse.setTotalElements(contacts.getTotalElements());
		contactsResponse.setTotalPages(contacts.getTotalPages());
		contactsResponse.setLast(contacts.isLast());
		return contactsResponse;
	}

	@Override
	public ContactDTO getContact(Long id) {
		return contactsRepository.findById(id).map(this::convertToDto).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", id));
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#id)")
	@Override
	public void deleteContact(Long id) {
		Contact contact = contactsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", id));
		contactsRepository.delete(contact);
	}

	@PreAuthorize("@contactAccess.canUpdateContact(#id)")
	@Override
	public ContactDTO updateContact(Long id, ContactDTO updatedContactDTO) {
		contactsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact", "id", id));
		Contact updatedContact = convertToEntity(updatedContactDTO);
		updatedContact.setId(id);
		return convertToDto(contactsRepository.save(updatedContact));
	}

	@Override
	public ContactDTO createContact(ContactDTO contactDTO) {
		Contact contact = convertToEntity(contactDTO);
		return convertToDto(contactsRepository.save(contact));
	}

	public ContactDTO convertToDto(Contact contact) {
		return ContactDTO.convertToDto(contact, modelMapper);
	}

	public Contact convertToEntity(ContactDTO contactDto) {
		return ContactDTO.convertToEntity(contactDto, modelMapper, passwordEncoder);
	}
}
