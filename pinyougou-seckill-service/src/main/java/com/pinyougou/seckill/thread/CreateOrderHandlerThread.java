package com.pinyougou.seckill.thread;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
@Scope("prototype")
public class CreateOrderHandlerThread implements Runnable{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;



    @Override
    public void run() {

        //先从队列中获取 秒杀商品的信息以及用户信息==》队列中元素包括userid，id
        RecordInfo info = (RecordInfo) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();

        if(info!=null){
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(info.getId());

            //创建订单成功，就弹出一个排队人
            redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX+info.getId()).rightPop();

            //3.减库存,再存储到redis中
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
            redisTemplate.boundHashOps("seckillGoods").put(info.getId(),seckillGoods);

            //4.若秒杀商品为0，删除redis中商品，更新数据库
            if(seckillGoods.getStockCount()<=0){
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps("seckillGoods").delete(info.getId());
            }

            //5.下预订单在redis中
            TbSeckillOrder order = new TbSeckillOrder();

            order.setId(new IdWorker(1,1).nextId());
            order.setSeckillId(info.getId());
            order.setMoney(seckillGoods.getCostPrice());
            order.setSellerId(seckillGoods.getSellerId());
            order.setCreateTime(new Date());
            order.setStatus("0");

            redisTemplate.boundHashOps("seckillOrder").put(info.getUserId(),order);

            //下单成功后删除排队标识
            redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(info.getUserId());
        }

    }
}
