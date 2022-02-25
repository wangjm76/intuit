package com.intuit.tradieBid.repository;

import com.intuit.tradieBid.entity.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface JobRepository extends CrudRepository<Job, Integer> {
    Job findByJobId(Integer id);

    @Query("select job from Job job where job.winningBidId is null")
    List<Job> findUncompletedJobs();

    @Modifying
    @Query("update Job set winningBidId = :winningBidId where jobId = :jobId")
    @Transactional
    void updateWinningBid(@Param("jobId") Integer jobId, @Param("winningBidId") Integer winningBidId);
}
