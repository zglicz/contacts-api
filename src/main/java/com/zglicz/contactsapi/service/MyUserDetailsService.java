package com.zglicz.contactsapi.service;

import com.zglicz.contactsapi.repositories.ContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
	@Autowired
	private ContactsRepository contactsRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return contactsRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
	}
}
