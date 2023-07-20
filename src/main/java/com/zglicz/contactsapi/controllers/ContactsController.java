package com.zglicz.contactsapi.controllers;

import com.zglicz.contactsapi.dto.ContactDTO;
import com.zglicz.contactsapi.dto.ContactsResponse;
import com.zglicz.contactsapi.service.ContactsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/contacts")
@Tag(name = "Contacts", description = "Contacts management main endpoint")
public class ContactsController {
    Logger logger = LoggerFactory.getLogger(ContactsController.class);

    public final static String CONTACT_DELETED_SUCCESS = "Contact deleted";
    public final static String INVALID_REQUEST_ERROR = "Provided content is invalid";
    public final String DEFAULT_PAGE_NO = "0";
    public final String DEFAULT_PAGE_SIZE = "10";

    private final ContactsService contactsService;

    public ContactsController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @Operation(summary = "Get a list of all contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully loaded all contacts",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ContactDTO.class))) })})
    @GetMapping(value = "/", params = {"page", "size"})
    public ResponseEntity<ContactsResponse> getContacts(
            @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NO, required = false) int pageNo,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize) {
        return ResponseEntity.ok(contactsService.getContacts(pageNo, pageSize));
    }

    @Operation(summary = "Get a single contact by id")
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(@PathVariable final Long id) {
        return new ResponseEntity<>(contactsService.getContact(id), HttpStatus.OK);
    }

    @Operation(summary = "Delete a contact by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(@PathVariable final Long id) {
        contactsService.deleteContact(id);
        return new ResponseEntity<>(CONTACT_DELETED_SUCCESS, HttpStatus.OK);
    }

    @Operation(summary = "Update a contact by id")
    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable final Long id, @Valid @RequestBody ContactDTO updatedContactDTO) {
        return new ResponseEntity<>(contactsService.updateContact(id, updatedContactDTO), HttpStatus.OK);
    }

    @Operation(summary = "Create a new contact")
    @PostMapping("/")
    ResponseEntity<ContactDTO> addContact(@Valid @RequestBody ContactDTO contactDto) {
        return new ResponseEntity<>(contactsService.createContact(contactDto), HttpStatus.OK);
    }
}
