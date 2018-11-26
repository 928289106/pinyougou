package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/submitOrder")
    public Result submitOrder(Long id){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userId.equals("anonymousUser")){
            return new Result(false,"401");
        }
        try {
            seckillOrderService.addOrder(id,userId);
            System.out.println(1);
            return new Result(true,"创建订单成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"创建订单失败");
        }
    }

    //每隔三秒就调用一次请求，查询订单状态
    @RequestMapping("/queryStatus")
    public Result queryStatus(){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userId.equals("anonymousUser")){
            return new Result(false,"401");
        }
        TbSeckillOrder seckillOrder = seckillOrderService.getFromRedis(userId);
        if(seckillOrder!=null){
            System.out.println(2);
            return new Result(true,"创建订单成功");
        }else {
            Boolean flag = seckillOrderService.isFlag(userId);
            if(flag){
                return new Result(false,"正在排队");
            }else {
                return new Result(false,"创建失败");
            }
        }
    }

}
