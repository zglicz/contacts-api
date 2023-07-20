package com.zglicz.contactsapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.dto.ContactSkillDTO;
import com.zglicz.contactsapi.entities.Contact;
import com.zglicz.contactsapi.entities.ContactSkill;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    private ObjectMapper objectMapper;

    public final static String CONTACT_DELETED_SUCCESS = "Contact deleted";
    public final static String SKILLS_UPDATED_SUCCESS = "Skills saved successfully";
    public final static String DUPLICATE_SKILLS_ERROR = "Duplicate skills provided for a single contact";
    public final static String ACCESS_DENIED_ERROR = "Not allowed to modify other users' data";
    public final static String INVALID_REQUEST_ERROR = "Provided content is invalid";

    private final ContactsRepository contactsRepository;
    private final ContactSkillsRepository contactSkillsRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public ContactsController(ContactsRepository contactsRepository, ContactSkillsRepository contactSkillsRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, ModelMapper modelMapper) {
        this.contactsRepository = contactsRepository;
        this.contactSkillsRepository = contactSkillsRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Get a list of all contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded all contacts",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Contact.class))) })})
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

    @Operation(summary = "Get a list of all skills for a contact id")
    @GetMapping("/{id}/skills")
    public ResponseEntity<List<ContactSkillDTO>> getContactSkills(@PathVariable final Long id) {
        return contactsRepository.findById(id)
                .map(contact -> new ResponseEntity<>(contactSkillsRepository.findByContactId(id).stream().map(this::convertToDto).collect(Collectors.toList()), HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("@contactAccess.canUpdateContact(#id)")
    @Operation(summary = "Update the list of skills for a contact id")
    @PostMapping("/{id}/skills")
    public ResponseEntity<String> updateContactSkills(
            @PathVariable final Long id, @Valid @RequestBody List<ContactSkillDTO> contactSkillDTOs) {
        return contactsRepository.findById(id)
                .map(contact -> {
                    contactSkillsRepository.deleteAll(contactSkillsRepository.findByContactId(id));
                    List<ContactSkill> contactSkills = contactSkillDTOs.stream().map(this::convertToEntity).collect(Collectors.toList());
                    contactSkills.stream().forEach(skillContact -> skillContact.setContact(contact));
                    logger.info("adding", contactSkills);
                    contactSkillsRepository.saveAll(contactSkills);
                    return ResponseEntity.ok(SKILLS_UPDATED_SUCCESS);
                })
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
    public ResponseEntity<Contact> updateContact(
            @PathVariable final Long id, @Valid @RequestBody ContactDTO updatedContactDTO) {
        return contactsRepository.findById(id)
                .map(contact -> {
                    Contact updatedContact = convertToEntity(updatedContactDTO);
                    updatedContact.setId(id);
                    return new ResponseEntity<>(contactsRepository.save(updatedContact), HttpStatus.OK);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
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

    public ContactDTO convertToDto(Contact contact) {
        return ContactDTO.convertToDto(contact, modelMapper);
    }

    public Contact convertToEntity(ContactDTO contactDto) {
        return ContactDTO.convertToEntity(contactDto, modelMapper, passwordEncoder);
    }

    public ContactSkillDTO convertToDto(ContactSkill contactSkill) {
        return ContactSkillDTO.convertToDto(contactSkill, modelMapper);
    }

    public ContactSkill convertToEntity(ContactSkillDTO contactSkillDTO) {
        return ContactSkillDTO.convertToEntity(contactSkillDTO, modelMapper);
    }
}
