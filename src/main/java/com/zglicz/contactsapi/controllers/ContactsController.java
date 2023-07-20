package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.repositories.ContactSkillsRepository;
import com.zglicz.contactsapi.repositories.ContactsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/contacts")
@Tag(name = "Contacts", description = "Contacts management main endpoint")
public class ContactsController {
    Logger logger = LoggerFactory.getLogger(ContactsController.class);

    public final static String CONTACT_DELETED_SUCCESS = "Contact deleted";
    public final static String INVALID_REQUEST_ERROR = "Provided content is invalid";

    private final ContactsRepository contactsRepository;
    private final ContactSkillsRepository contactSkillsRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public ContactsController(ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.contactsRepository = contactsRepository;
        this.contactSkillsRepository = contactSkillsRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Get a list of all contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded all contacts",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ContactDTO.class))) })})
    @GetMapping("/")
    public ResponseEntity<List<ContactDTO>> getContacts() {
        List<Contact> contacts = (List<Contact>) contactsRepository.findAll();
        return ResponseEntity.ok(contacts.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Get a single contact by id")
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(@PathVariable final Long id) {
        return contactsRepository.findById(id)
                .map(contact -> new ResponseEntity<>(convertToDto(contact), HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("@contactAccess.canUpdateContact(#id)")
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

    @PreAuthorize("@contactAccess.canUpdateContact(#id)")
    @Operation(summary = "Update a contact by id")
    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable final Long id, @Valid @RequestBody ContactDTO updatedContactDTO) {
        return contactsRepository.findById(id)
                .map(contact -> {
                    Contact updatedContact = convertToEntity(updatedContactDTO);
                    updatedContact.setId(id);
                    return new ResponseEntity<>(convertToDto(contactsRepository.save(updatedContact)), HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new contact")
    @PostMapping("/")
    ResponseEntity<ContactDTO> addContact(@Valid @RequestBody ContactDTO contactDto) {
        Contact contact = convertToEntity(contactDto);
        Contact savedContact = contactsRepository.save(contact);
        return ResponseEntity.ok(convertToDto(savedContact));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleValidationExceptions(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation cv : ex.getConstraintViolations()) {
            String fieldName = ((PathImpl)cv.getPropertyPath()).getLeafNode().getName();
            String message = cv.getMessage();
            errors.put(fieldName, message);
        }
        return errors;
    }

    public ContactDTO convertToDto(Contact contact) {
        return ContactDTO.convertToDto(contact, modelMapper);
    }

    public Contact convertToEntity(ContactDTO contactDto) {
        return ContactDTO.convertToEntity(contactDto, modelMapper, passwordEncoder);
    }
}
