package com.intuit.tradieBid.controller;

import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.entity.Tradie;
import com.intuit.tradieBid.model.WinningBid;
import com.intuit.tradieBid.service.TradieBidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class TradieBidController {

    private final TradieBidService tradieBidService;

    TradieBidController(TradieBidService tradieBidService) {
        this.tradieBidService = tradieBidService;
    }

    @PostMapping("/customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return new ResponseEntity<>(tradieBidService.createCustomer(customer), HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Customer> findCustomer(@PathVariable Integer customerId) {
        return new ResponseEntity<>(tradieBidService.findCustomer(customerId), HttpStatus.OK);
    }

    @PostMapping("/tradie")
    public ResponseEntity<Tradie> createTradie(@RequestBody Tradie tradie) {
        return new ResponseEntity<>(tradieBidService.createTradie(tradie), HttpStatus.CREATED);
    }

    @GetMapping("/tradie/{tradieId}")
    public ResponseEntity<Tradie> findTradie(@PathVariable Integer tradieId) {
        return new ResponseEntity<>(tradieBidService.findTradie(tradieId), HttpStatus.OK);
    }

    @PostMapping("/job")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return new ResponseEntity<>(tradieBidService.createJob(job), HttpStatus.CREATED);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<Job> findJob(@PathVariable Integer jobId) {
        return new ResponseEntity<>(tradieBidService.findJob(jobId), HttpStatus.OK);
    }

    @GetMapping("/job/{jobId}/winning-bid")
    public ResponseEntity<WinningBid> getWinningBid(@PathVariable Integer jobId) {
        return new ResponseEntity<>(tradieBidService.getWinningBid(jobId), HttpStatus.OK);
    }

    @PostMapping("/bid")
    public ResponseEntity<Bid> createBid(@RequestBody Bid bid) {
        return new ResponseEntity<>(tradieBidService.createBid(bid), HttpStatus.CREATED);
    }

    @GetMapping("/bid/{bidId}")
    public ResponseEntity<Bid> findBid(@PathVariable Integer bidId) {
        return new ResponseEntity<>(tradieBidService.findBid(bidId), HttpStatus.OK);
    }

}
