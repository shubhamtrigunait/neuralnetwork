package com.kunal.neuralnetwork.persistence;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CassandraRepository<OrderModel, String> {
    OrderModel findByOrderNumber(String orderNumber);
}
