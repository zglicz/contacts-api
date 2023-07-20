package com.zglicz.contactsapi.misc;

import com.zglicz.contactsapi.entities.Contact;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ResponseExceptionHandler {
	public final static String DUPLICATE_SKILLS_ERROR = "Duplicate skills provided for a single contact";
	public final static String ACCESS_DENIED_ERROR = "Not allowed to modify other users' data";

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value=MethodArgumentNotValidException.class)
	public Map<String, String> handleException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		return errors;
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleValidationExceptions(DataIntegrityViolationException ex) {
		String exMessage = ex.getMostSpecificCause().getMessage();
		if (exMessage.contains("PUBLIC.CONTACT_SKILL(CONTACT_ID NULLS FIRST, SKILL_ID NULLS FIRST)")) {
			return DUPLICATE_SKILLS_ERROR;
		} else if (exMessage.contains("PUBLIC.CONTACT(EMAIL NULLS FIRST)")) {
			return Contact.EMAIL_DUPLICATE_ERROR;
		} else {
			return Contact.UNKNOWN_ERROR;
		}
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(AccessDeniedException.class)
	public String handleAccessDeniedException(AccessDeniedException ex) {
		return ACCESS_DENIED_ERROR;
	}
}
