package de.ksul.archiv.model.comments;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

public class Person {

    String id = UUID.randomUUID().toString();
    String firstName;
    String lastName;
    String description;
    String avatarId;
    String email;
    String skypeId;
    String googleId;
    String instantMessageId;
    String jobTitle;
    String location;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Company company;
    String mobile;
    String telephone;
    String statusUpdatedAt;
    String userStatus;
    boolean enabled = true;
    boolean emailNotificationsEnabled = true;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> aspectNames;
    Object properties;
    Object capabilities;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSkypeId() {
        return skypeId;
    }

    public void setSkypeId(String skypeId) {
        this.skypeId = skypeId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getInstantMessageId() {
        return instantMessageId;
    }

    public void setInstantMessageId(String instantMessageId) {
        this.instantMessageId = instantMessageId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(String statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public List<String> getAspectNames() {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames) {
        this.aspectNames = aspectNames;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    public Object getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Object capabilities) {
        this.capabilities = capabilities;
    }
}
