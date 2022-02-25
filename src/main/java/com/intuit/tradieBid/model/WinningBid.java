package com.intuit.tradieBid.model;

import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Tradie;

public class WinningBid {
    private Integer jobId;
    private String description;
    private Integer cost;
    private Customer customer;
    private Tradie tradie;

    public WinningBid(Integer jobId, String description, Integer cost, Customer customer, Tradie tradie) {
        this.jobId = jobId;
        this.description = description;
        this.cost = cost;
        this.customer = customer;
        this.tradie = tradie;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Tradie getTradie() {
        return tradie;
    }

    public void setTradie(Tradie tradie) {
        this.tradie = tradie;
    }
}
