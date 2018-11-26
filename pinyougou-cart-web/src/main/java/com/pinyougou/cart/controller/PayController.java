package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WxPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WxPayService wxPayService;

    @Reference
    private OrderService orderService;

    //生成支付内容集合
    @RequestMapping("/createNative")
    public Map createNative(){

        /*生产订单
        String out_trade_no = new IdWorker(0, 1).nextId()+"";*/


        TbPayLog tbPayLog = orderService.getTbPayLogFromRedis(SecurityContextHolder.getContext().getAuthentication().getName());
        if(tbPayLog==null){
            return null;
        }

        Map map = wxPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");
        return map;
    }

    //查询支付结果
    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){

        Result result = new Result(false,"支付失败");
        int i=0;
        while (true){

            i++;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Map map = wxPayService.queryStatus(out_trade_no);
            if(i>5){
                result = new Result(false,"支付超时");
                break;
            }
            if(map==null) {
                result = new Result(false,"支付失败");
                break;
            }

            if("SUCCESS".equals(map.get("trade_state"))) {

                //更新订单状态,更新支付日志状态，更新微信支付id,删除redis中数据
                orderService.updateTbPayLogAndOrder(out_trade_no, (String) map.get("transation_id"));


                result = new Result(true, "支付成功");
                break;
            }
        }
        return result;
    }

}
