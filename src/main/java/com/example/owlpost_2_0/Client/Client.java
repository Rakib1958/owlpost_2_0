package com.example.owlpost_2_0.Client;

import java.time.LocalDate;
import java.util.Objects;

public class Client {
    private String username;
    private String password;
    private String email;
    private LocalDate dateofbirth;
    private String house;
    private String patronus;
    private String profilePicturePath;

    public Client() {}

    public Client(String username, String patronus, String house, LocalDate dateofbirth, String email, String password) {
        this.username = username;
        this.patronus = patronus;
        this.house = house;
        this.dateofbirth = dateofbirth;
        this.email = email;
        this.password = password;
    }

    public Client(String username, String password, String email, LocalDate dateofbirth) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.dateofbirth = dateofbirth;
    }

    public Client(Client other) {
        this.username = other.username;
        this.patronus = other.patronus;
        this.house = other.house;
        this.dateofbirth = other.dateofbirth;
        this.email = other.email;
        this.password = other.password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getPatronus() {
        return patronus;
    }

    public void setPatronus(String patronus) {
        this.patronus = patronus;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }


    public int getAge() {
        if (dateofbirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateofbirth.getYear();
    }

    public boolean underAge() {
        return getAge() > 18;
    }

    public boolean isValidForRegistration() {
        return (username != null && password != null && email != null && dateofbirth != null && house != null && patronus != null);
    }

    public boolean isValidForSorting() {
        return (username != null && password != null && email != null && dateofbirth != null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Client client = (Client) obj;
        return Objects.equals(username, client.username);
    }

    @Override
    public String toString() {
        return "Client{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", dateofbirth=" + dateofbirth +
                ", house='" + house + '\'' +
                ", patronus='" + patronus + '\'' +
                ", profilePicPath='" + profilePicturePath + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public void setProfilePicPath(String profilepic) {
        this.profilePicturePath = profilepic;
    }
}