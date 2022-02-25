package com.intuit.tradieBid;

import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.entity.Tradie;
import com.intuit.tradieBid.model.WinningBid;

import java.time.OffsetDateTime;

public class TestData {
    public final static Customer customer = new Customer(
            "James",
            "Wang",
            "james.wang@test.com",
            "04222222222"
    );

    public final static Tradie tradieOne = new Tradie(
            "Tradie",
            "One",
            "tradieOne@test.com",
            "04222222222"
    );

    public final static Tradie tradieTwo = new Tradie(
            "Tradie",
            "Two",
            "tradieTwo@test.com",
            "04222222222"
    );

    public final static Job jobOne = new Job(
            1,
            "Job1",
            2,
            OffsetDateTime.now().plusHours(1)
    );

    public final static Bid bidOne = new Bid(
            1,
            1,
            500,
            null
    );

    public final static Bid bidTwo = new Bid(
            1,
            2,
            null,
            50
    );

    public final static WinningBid winningBidResult = new WinningBid(
            1004,
            "Job1",
            100,
            customer,
            tradieTwo
    );
}
