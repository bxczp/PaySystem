package com.bxczp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bxczp.entity.Order;
import com.bxczp.service.OrderService;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @RequestMapping("/toList")
    public ModelAndView toList() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "订单查询");
        modelAndView.setViewName("orderList");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping("/orderList")
    public Map<String, Object> orderList(@RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Map<String, Object> resultMap = new HashMap<>();
        Order s_order = new Order();
        List<Order> orderList = orderService.list(s_order, page, limit);
        Long count = orderService.getListCount(s_order);
        // code 是否成功执行的标志
        resultMap.put("code", 0);
        // 总记录数
        resultMap.put("count", count);
        // 数据
        resultMap.put("data", orderList);
        return resultMap;
    }

}
