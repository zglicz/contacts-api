package com.zglicz.contactsapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;

@Entity
public class Contact {
    public final static String FIRSTNAME_REQUIRED_ERROR = "Firstname is required";
    public final static String LASTNAME_REQUIRED_ERROR = "Lasttname is required";
    public final static String EMAIL_NOT_BLANK_ERROR = "Email must not be empty";
    public final static String EMAIL_INVALID_ERROR = "Invalid email address";
    public final static String EMAIL_DUPLICATE_ERROR = "Email already exists";
    public final static String UNKNOWN_ERROR = "Unknown error occured";

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = FIRSTNAME_REQUIRED_ERROR)
    @Size(min = 2, max = 50)
    private String firstname;

    @NotBlank(message = LASTNAME_REQUIRED_ERROR)
    @Size(min = 2, max = 50)
    private String lastname;

    private String address;

    @Size(min = 8, max = 20)
    private String phoneNumber;

    @NotBlank(message = EMAIL_NOT_BLANK_ERROR)
    @Email(message = EMAIL_INVALID_ERROR)
    @Column(unique=true)
    private String email;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}