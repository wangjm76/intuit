package com.intuit.tradieBid.repository;

import com.intuit.tradieBid.entity.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    Customer findByCustomerId(Integer id);
}
