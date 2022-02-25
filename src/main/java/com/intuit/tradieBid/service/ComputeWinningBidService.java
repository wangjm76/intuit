package com.intuit.tradieBid.service;


import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.repository.BidRepository;
import com.intuit.tradieBid.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ComputeWinningBidService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JobRepository jobRepository;
    private final BidRepository bidRepository;

    public ComputeWinningBidService(JobRepository jobRepository, BidRepository bidRepository) {
        this.jobRepository = jobRepository;
        this.bidRepository = bidRepository;
    }

    public Runnable createTask(final Job job) {
        logger.info("create scheduling for job " + job.getJobId());

        return () -> computeWinningBid(job);
    }

    // find all the bids for the job, and sort them by cost to find the winning bid, then update db
    public void computeWinningBid(final Job job) {
        logger.info("find all bids for job" + job.getJobId());
        List<Bid> allBids = bidRepository.findAllByJobId(job.getJobId());

        // in case no bid for this job, update the winning bid as -1 so it won't be picked for scheduling when app restarts
        if (allBids.isEmpty()) {
            jobRepository.updateWinningBid(job.getJobId(), -1);
        } else {
            allBids.sort(Comparator.comparing(bid -> bid.getCost(job.getExpectedHours())));
            jobRepository.updateWinningBid(job.getJobId(), allBids.get(0).getBidId());
        }
    }
}
