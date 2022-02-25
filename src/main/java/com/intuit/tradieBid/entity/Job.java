package com.intuit.tradieBid.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jobId;
    private Integer customerId;
    private String description;
    private Integer expectedHours;
    private Integer winningBidId = null;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime toBeBiddenBy;

    public Job(Integer customerId, String description, Integer expectedHours, OffsetDateTime toBeBiddenBy) {
        this.customerId = customerId;
        this.description = description;
        this.expectedHours = expectedHours;
        this.toBeBiddenBy = toBeBiddenBy;
    }

    public Job(Job another) {
        this.customerId = another.customerId;
        this.description = another.description;
        this.expectedHours = another.expectedHours;
        this.toBeBiddenBy = another.toBeBiddenBy;
    }

    protected Job() {
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExpectedHours() {
        return expectedHours;
    }

    public void setExpectedHours(Integer expectedHours) {
        this.expectedHours = expectedHours;
    }

    public Integer getWinningBidId() {
        return winningBidId;
    }

    public void setWinningBidId(Integer winningTradieId) {
        this.winningBidId = winningTradieId;
    }

    public OffsetDateTime getToBeBiddenBy() {
        return toBeBiddenBy;
    }

    public void setToBeBiddenBy(OffsetDateTime toBeBiddenBy) {
        this.toBeBiddenBy = toBeBiddenBy;
    }


    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }
}
