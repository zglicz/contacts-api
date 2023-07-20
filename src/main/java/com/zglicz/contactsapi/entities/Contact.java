package com.zglicz.contactsapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
public class Contact implements UserDetails {
    public final static String EMAIL_NOT_BLANK_ERROR = "Email must not be empty";
    public final static String EMAIL_INVALID_ERROR = "Invalid email address";
    public final static String EMAIL_DUPLICATE_ERROR = "Email already exists";
    public final static String UNKNOWN_ERROR = "Unknown error occurred";
    public final static String FIRSTNAME_LENGTH_ERROR = "Invalid length of firstname";
    public final static String LASTNAME_LENGTH_ERROR = "Invalid length of lastname";
    public final static String PASSWORD_NOT_EMPTY_ERROR = "Password cannot be empty";

    @Id
    @GeneratedValue
    private Long id;

    @Size(min = 2, max = 50, message = FIRSTNAME_LENGTH_ERROR)
    private String firstname;

    @Size(min = 2, max = 50, message = LASTNAME_LENGTH_ERROR)
    private String lastname;

    private String address;

    @Size(min = 8, max = 20)
    private String phoneNumber;

    @NotBlank(message = EMAIL_NOT_BLANK_ERROR)
    @Email(message = EMAIL_INVALID_ERROR)
    @Column(unique=true)
    private String email;

    @NotBlank(message = PASSWORD_NOT_EMPTY_ERROR)
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @OneToMany(mappedBy = "contact")
    private Set<ContactSkill> skills;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

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

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}