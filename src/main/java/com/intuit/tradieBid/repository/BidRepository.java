package com.intuit.tradieBid.repository;

import com.intuit.tradieBid.entity.Bid;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BidRepository extends CrudRepository<Bid, Integer> {
    List<Bid> findAllByJobId(Integer id);

    Bid findByBidId(Integer Id);
}
