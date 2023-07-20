package com.zglicz.contactsapi.dto;

import com.zglicz.contactsapi.entities.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ContactDTO {
	private Long id;
	private String firstname;
	private String lastname;
	private String email;
	private String address;
	private String phoneNumber;
	private String plainPassword;

	public static ContactDTO convertToDto(Contact contact, ModelMapper modelMapper) {
		ContactDTO contactDto = modelMapper.map(contact, ContactDTO.class);
		return contactDto;
	}

	public static Contact convertToEntity(ContactDTO contactDto, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
		Contact contact = modelMapper.map(contactDto, Contact.class);
		contact.setPassword(passwordEncoder.encode(contactDto.getPlainPassword()));
		return contact;
	}
}
