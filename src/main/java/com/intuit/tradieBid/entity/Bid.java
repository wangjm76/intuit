package com.intuit.tradieBid.entity;

import org.springframework.util.ObjectUtils;

import javax.persistence.*;

@Entity
@Table(name = "bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bidId;
    private Integer jobId;
    private Integer tradieId;
    private Integer fixedPrice;
    private Integer hourlyRate;

    public Bid(Integer jobId, Integer tradieId, Integer fixedPrice, Integer hourlyRate) {
        this.jobId = jobId;
        this.tradieId = tradieId;
        this.fixedPrice = fixedPrice;
        this.hourlyRate = hourlyRate;
    }

    protected Bid() {
    }

    public Bid(Bid another) {
        this.jobId = another.jobId;
        this.tradieId = another.tradieId;
        this.fixedPrice = another.fixedPrice;
        this.hourlyRate = another.hourlyRate;
    }

    public Integer getBidId() {
        return bidId;
    }

    public void setBidId(Integer bidId) {
        this.bidId = bidId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getTradieId() {
        return tradieId;
    }

    public void setTradieId(Integer tradieId) {
        this.tradieId = tradieId;
    }

    public Integer getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(Integer fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Integer getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Integer hourly_rate) {
        this.hourlyRate = hourly_rate;
    }

    public Integer getCost(Integer hour) {
        if (ObjectUtils.isEmpty(fixedPrice)) return hourlyRate * hour;
        else return fixedPrice;
    }

}
