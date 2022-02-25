package com.intuit.tradieBid.entity;

import javax.persistence.*;

@Entity
@Table(name = "tradies")
public class Tradie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tradieId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;

    protected Tradie() {
    }

    public Tradie(String firstName, String lastName, String email, String mobile) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
    }

    public Integer getTradieId() {
        return tradieId;
    }

    public void setTradieId(Integer tradieId) {
        this.tradieId = tradieId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
