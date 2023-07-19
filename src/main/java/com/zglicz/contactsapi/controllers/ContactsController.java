package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
import com.zglicz.contactsapi.misc.UniqueSkillsConstraint;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/contacts")
@Tag(name = "Contacts", description = "Contacts management main endpoint")
public class ContactsController {
    Logger logger = LoggerFactory.getLogger(ContactsController.class);

    @Autowired
    private ObjectMapper objectMapper;

    public final static String CONTACT_DELETED_SUCCESS = "Contact deleted";
    public final static String SKILLS_UPDATED_SUCCESS = "Skills saved successfully";
    public final static String DUPLICATE_SKILLS_ERROR = "Duplicate skills provided for a single contact";

    private final ContactsRepository contactsRepository;
    private final ContactSkillsRepository contactSkillsRepository;

    @Autowired
    public ContactsController(ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository) {
        this.contactsRepository = contactsRepository;
        this.contactSkillsRepository = contactSkillsRepository;
    }

    @Operation(summary = "Get a list of all contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded all contacts",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Contact.class))) })})
    @GetMapping("/")
    public ResponseEntity<List<Contact>> getContacts() {
        return ResponseEntity.ok((List<Contact>) contactsRepository.findAll());
    }

    @Operation(summary = "Get a single contact by id")
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable final Long id) {
        return contactsRepository.findById(id)
                .map(contact -> new ResponseEntity<>(contact, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a list of all skills for a contact id")
    @GetMapping("/{id}/skills")
    public ResponseEntity<List<ContactSkill>> getContactSkills(@PathVariable final Long id) {
        return contactsRepository.findById(id)
                .map(contact -> new ResponseEntity<>(contactSkillsRepository.findByContactId(id), HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update the list of skills for a contact id")
    @PostMapping("/{id}/skills")
    public ResponseEntity<String> updateContactSkills(
            @PathVariable final Long id, @Valid @RequestBody @UniqueSkillsConstraint List<ContactSkill> skills) {
        return contactsRepository.findById(id)
                .map(contact -> {
                    try {
                        skills.stream().forEach(skill -> skill.setContact(contact));
                        contactSkillsRepository.deleteAll();
                        logger.info(objectMapper.writeValueAsString(skills));
                        contactSkillsRepository.saveAll(skills);
                    } catch (Exception e) {

                    }
                    return ResponseEntity.ok(SKILLS_UPDATED_SUCCESS);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a contact by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(@PathVariable final Long id) {
        return contactsRepository.findById(id)
                .map(contact -> {
                    contactsRepository.deleteById(id);
                    return new ResponseEntity<>(CONTACT_DELETED_SUCCESS, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a contact by id")
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable final Long id, @Valid @RequestBody Contact updatedContact) {
        updatedContact.setId(id);
        return contactsRepository.findById(id)
                .map(contact -> new ResponseEntity<>(contactsRepository.save(updatedContact), HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new contact")
    @PostMapping("/")
    ResponseEntity<Contact> addContact(@Valid @RequestBody Contact contact) {
        Contact savedContact = contactsRepository.save(contact);
        return ResponseEntity.ok(savedContact);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleValidationExceptions(ConstraintViolationException ex) {
        return ContactsController.DUPLICATE_SKILLS_ERROR;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
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
    public String handleValidationExceptions(DataIntegrityViolationException ex) throws Exception{
        String exMessage = ex.getMostSpecificCause().getMessage();
        if (exMessage.contains("Unique index or primary key violation")) {
            return Contact.EMAIL_DUPLICATE_ERROR;
        } else {
            return Contact.UNKNOWN_ERROR;
        }
    }
}
