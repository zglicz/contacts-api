package com.zglicz.contactsapi.service;

import com.zglicz.contactsapi.entities.Contact;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {
	public Contact getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (Contact) authentication.getPrincipal();
	}
}
