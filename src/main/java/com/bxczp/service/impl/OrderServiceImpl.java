package com.bxczp.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bxczp.entity.Order;
import com.bxczp.repository.OrderRepository;
import com.bxczp.service.OrderService;
import com.bxczp.util.StringUtil;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderRepository orderRepository;

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Override
    public List<Order> list(Order order, int page, int pageSize) {
        Pageable pageable = new PageRequest(page, pageSize, Sort.Direction.DESC, "buyTime");
        return orderRepository.findAll(new Specification<Order>() {

            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                if (order != null) {
                    if (StringUtil.isNotEmpty(order.getOrderNo())) {
                        predicate.getExpressions()
                                .add(cb.like(root.get("orderNo"), "%" + order.getOrderNo().trim() + "%"));
                    }
                    if (order.getIsPay() == 1) {
                        predicate.getExpressions().add(cb.equal(root.get("isPay"), order.getIsPay()));
                    }
                }
                return predicate;
            }
        }, pageable).getContent();
    }

    @Override
    public Order getByOrderNo(String out_trade_no) {
        return orderRepository.getByOrderNo(out_trade_no);
    }

    @Override
    public Order getById(int parseInt) {
        return orderRepository.findOne(parseInt);
    }

    @Override
    public long getListCount(Order order) {
        return orderRepository.count(new Specification<Order>() {

            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                if (order != null) {
                    if (StringUtil.isNotEmpty(order.getOrderNo())) {
                        predicate.getExpressions()
                                .add(cb.like(root.get("orderNo"), "%" + order.getOrderNo().trim() + "%"));
                    }
                    if (order.getIsPay() == 1) {
                        predicate.getExpressions().add(cb.equal(root.get("isPay"), order.getIsPay()));
                    }
                }
                return predicate;
            }
        });
    }

}
