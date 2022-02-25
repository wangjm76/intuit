package com.intuit.tradieBid.service;

import com.intuit.tradieBid.entity.Bid;
import com.intuit.tradieBid.entity.Customer;
import com.intuit.tradieBid.entity.Job;
import com.intuit.tradieBid.entity.Tradie;
import com.intuit.tradieBid.exception.InvalidBidException;
import com.intuit.tradieBid.exception.NoMoreBidException;
import com.intuit.tradieBid.exception.NoWinningBidException;
import com.intuit.tradieBid.exception.NotFoundException;
import com.intuit.tradieBid.model.WinningBid;
import com.intuit.tradieBid.repository.BidRepository;
import com.intuit.tradieBid.repository.CustomerRepository;
import com.intuit.tradieBid.repository.JobRepository;
import com.intuit.tradieBid.repository.TradieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TradieBidService {
    private final CustomerRepository customerRepository;
    private final TradieRepository tradieRepository;
    private final JobRepository jobRepository;
    private final BidRepository bidRepository;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final ComputeWinningBidService computeWinningBidService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TradieBidService(CustomerRepository customerRepository, TradieRepository tradieRepository, JobRepository jobRepository, BidRepository bidRepository, ThreadPoolTaskScheduler threadPoolTaskScheduler, ComputeWinningBidService computeWinningBidService) {
        this.customerRepository = customerRepository;
        this.tradieRepository = tradieRepository;
        this.jobRepository = jobRepository;
        this.bidRepository = bidRepository;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.computeWinningBidService = computeWinningBidService;
    }


    // in case app restarts, we want to reschedule jobs that are without a winning bid
    @PostConstruct
    public void scheduleAllUncompletedJobs() {
        logger.info("schedule all uncompleted jobs");
        jobRepository.findUncompletedJobs().forEach(this::scheduleProcessWinningBid);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }


    public Customer findCustomer(Integer customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    public Tradie createTradie(Tradie tradie) {
        return tradieRepository.save(tradie);
    }

    public Tradie findTradie(Integer tradieId) {
        return tradieRepository.findByTradieId(tradieId);
    }

    public Job createJob(Job job) {
        final Job savedJob = jobRepository.save(job);
        scheduleProcessWinningBid(savedJob);
        return savedJob;
    }

    public Job findJob(Integer jobId) {
        return jobRepository.findByJobId(jobId);
    }

    public Bid createBid(Bid bid) {
        final Job job = jobRepository.findByJobId(bid.getJobId());
        if (ObjectUtils.isEmpty(job)) {
            throw new NotFoundException();
        }
        // bid after job is expired is not allowed
        if (job.getToBeBiddenBy().isBefore(OffsetDateTime.now())) {
            throw new NoMoreBidException();
        }
        // bid should have either fix price or hourly rate
        if ((ObjectUtils.isEmpty(bid.getFixedPrice()) && ObjectUtils.isEmpty(bid.getHourlyRate())) ||
                !ObjectUtils.isEmpty(bid.getFixedPrice()) && !ObjectUtils.isEmpty(bid.getHourlyRate())) {
            throw new InvalidBidException();
        }
        return bidRepository.save(bid);
    }

    public Bid findBid(Integer bidId) {
        return bidRepository.findByBidId(bidId);
    }

    public WinningBid getWinningBid(Integer jobId) {

        final Job job = jobRepository.findByJobId(jobId);
        if (ObjectUtils.isEmpty(job)) {
            throw new NotFoundException();
        }
        if (ObjectUtils.isEmpty(job.getWinningBidId()) || job.getWinningBidId() == -1) {
            throw new NoWinningBidException();
        }
        final Bid winningBid = bidRepository.findByBidId(job.getWinningBidId());
        final Tradie tradie = tradieRepository.findByTradieId(winningBid.getTradieId());
        final Customer customer = customerRepository.findByCustomerId(job.getCustomerId());

        return new WinningBid(
                job.getJobId(),
                job.getDescription(),
                winningBid.getCost(job.getExpectedHours()),
                customer,
                tradie
        );
    }

    public void scheduleProcessWinningBid(Job job) {
        logger.info("schedule computing winnning bid for job:[jobId:" + job.getJobId() + "] at" + job.getToBeBiddenBy().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        threadPoolTaskScheduler.schedule(computeWinningBidService.createTask(job), Instant.parse(job.getToBeBiddenBy().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

}
