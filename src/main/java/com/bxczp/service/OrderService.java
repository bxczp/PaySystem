package com.bxczp.service;

import java.util.List;

import com.bxczp.entity.Order;

public interface OrderService {
    
    
    public void save(Order order);
    
    
    public List<Order> list(Order order, int page, int pageSize);
    
    public long getListCount (Order order);


    public Order getByOrderNo(String out_trade_no);


    public Order getById(int parseInt);
    
    

}
