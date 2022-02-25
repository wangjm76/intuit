package com.intuit.tradieBid.repository;

import com.intuit.tradieBid.entity.Tradie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TradieRepository extends CrudRepository<Tradie, Integer> {
    Tradie findByTradieId(Integer id);
}
