package com.bxczp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.bxczp.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    
    @Query(value="select * from t_order where order_no = ?1 ", nativeQuery=true)
    public Order getByOrderNo(String out_trade_no);
    
}
