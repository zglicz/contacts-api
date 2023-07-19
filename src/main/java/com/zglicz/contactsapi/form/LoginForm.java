package com.zglicz.contactsapi.form;

import jakarta.validation.constraints.NotBlank;

public class LoginForm {
	@NotBlank
	private String username;
	@NotBlank
	private String password;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}