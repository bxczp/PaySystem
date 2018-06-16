package com.bxczp.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.bxczp.entity.Order;
import com.bxczp.properties.AlipayProperties;
import com.bxczp.service.OrderService;
import com.bxczp.util.DateUtil;
import com.bxczp.util.DeviceUtil;
import com.bxczp.util.StringUtil;

@Controller
@RequestMapping("/alipay")
public class AliPayController {

    @Resource
    private AlipayProperties aliPayProperties;

    private static Logger logger = Logger.getLogger(AliPayController.class);

    @Resource
    private OrderService orderService;

    @RequestMapping("/pay")
    public void pay(Order order, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String orderNo = DateUtil.getCurrentDateStr(); // 生成订单号

        String totalAmount = ""; // 支付总金额
        String subject = ""; // 订单名称
        String body = ""; // 商品描述
        switch (order.getProductId()) {
        case 1:
            totalAmount = "0.01";
            subject = "小意思";
            body = "0.01元-小意思";
            break;
        case 2:
            totalAmount = "0.02";
            subject = "中意思";
            body = "0.02元-中意思";
            break;
        case 3:
            totalAmount = "0.03";
            subject = "大意思";
            body = "0.03元-大意思";
            break;
        default:
            totalAmount = "0.01";
            subject = "小意思";
            body = "0.01元-小意思";
            break;
        }
        order.setOrderNo(orderNo);
        order.setSubject(subject);
        order.setTotalAmount(totalAmount);
        order.setBody(body);

        orderService.save(order);

        // 封装请求客户端
        AlipayClient client = new DefaultAlipayClient(aliPayProperties.getUrl(), aliPayProperties.getAppid(),
                aliPayProperties.getRsa_private_key(), aliPayProperties.getFormat(), aliPayProperties.getCharset(),
                aliPayProperties.getAlipay_public_key(), aliPayProperties.getSigntype());

        String form = ""; // 生成的支付表单

        String userAgent = request.getHeader("user-agent");

        if (DeviceUtil.isMobileDevice(userAgent)) { // 移动设备
            AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
            alipayRequest.setReturnUrl(aliPayProperties.getReturn_url());
            alipayRequest.setNotifyUrl(aliPayProperties.getNotify_url());
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 设置销售产品码
            model.setOutTradeNo(orderNo); // 设置订单号
            model.setSubject(subject); // 订单名称
            model.setTotalAmount(totalAmount); // 支付总金额
            model.setBody(body); // 设置商品描述
            alipayRequest.setBizModel(model);
            form = client.pageExecute(alipayRequest).getBody(); // 生成表单
            
            
        } else {

            // 支付请求
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(aliPayProperties.getReturn_url());
            alipayRequest.setNotifyUrl(aliPayProperties.getNotify_url());
            AlipayTradePayModel model = new AlipayTradePayModel();
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 设置销售产品码
            model.setOutTradeNo(orderNo); // 设置订单号
            model.setSubject(subject); // 订单名称
            model.setTotalAmount(totalAmount); // 支付总金额
            model.setBody(body); // 设置商品描述
            alipayRequest.setBizModel(model);

            form = client.pageExecute(alipayRequest).getBody(); // 生成表单

            response.setContentType("text/html;charset=" + aliPayProperties.getCharset());
            response.getWriter().write(form); // 直接将完整的表单html输出到页面
            response.getWriter().flush();
            response.getWriter().close();
        }

    }

    /**
     * 支付宝服务器异步通知
     * 
     * @param request
     * @throws Exception
     */
    @RequestMapping("/notifyUrl")
    public void notifyUrl(HttpServletRequest request) throws Exception {
        logger.info("异步通知notifyUrl");
        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
            logger.info("name:" + name + ",valueStr:" + valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayProperties.getAlipay_public_key(),
                aliPayProperties.getCharset(), aliPayProperties.getSigntype()); // 调用SDK验证签名
        // 商户订单号
        String out_trade_no = request.getParameter("out_trade_no");
        // 交易状态
        String trade_status = request.getParameter("trade_status");

        if (signVerified) { // 验证成功 更新订单信息
            if (trade_status.equals("TRADE_FINISHED")) {
                logger.info("TRADE_FINISHED");
            }
            if (trade_status.equals("TRADE_SUCCESS")) {
                logger.info("TRADE_SUCCESS");
            }
            if (StringUtil.isNotEmpty(out_trade_no)) {
                Order order = orderService.getByOrderNo(out_trade_no);
                if (order != null) {
                    order.setBuyTime(new Date()); // 支付时间
                    order.setIsPay(1); // 支付支付状态 已经支付
                    orderService.save(order);
                }
            }
        } else {
            logger.error("验证未通过");
        }
    }

    /**
     * 同步跳转
     * 
     * @param request
     * @throws Exception
     */
    @RequestMapping("/returnUrl")
    public ModelAndView returnUrl(HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject("title", "同步通知地址");

        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
            logger.info("name:" + name + ",valueStr:" + valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayProperties.getAlipay_public_key(),
                aliPayProperties.getCharset(), aliPayProperties.getSigntype()); // 调用SDK验证签名

        if (signVerified) {
            // 验证通过
            mav.addObject("message", "非常感谢");
        } else {
            mav.addObject("message", "验签失败");
        }
        mav.setViewName("returnUrl");
        return mav;
    }

}
