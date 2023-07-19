package com.zglicz.contactsapi.exceptions;

public class AppException extends RuntimeException {
	private int code;

	public AppException(String message) {
		this(message, 400);
	}

	public AppException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}