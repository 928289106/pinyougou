package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordInfo;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.thread.CreateOrderHandlerThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CreateOrderHandlerThread createOrderHandlerThread;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void addOrder(Long id, String userId) {

        //1.从redis中根据商品id，获取商品信息
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);

        //2.1判断商品是否已经售罄
        /*if(seckillGoods==null || seckillGoods.getStockCount()<=0){
            throw new RuntimeException("商品售罄");
        }*/
        Object o = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + id).rightPop();
        if(o==null){
            throw new RuntimeException("商品售罄");
        }
        //2.2判断用户是否有未支付的订单
        Boolean flag = redisTemplate.boundHashOps("seckillOrder").hasKey(userId);
        if(flag){
            throw new RuntimeException("您有未支付的订单");
        }

        //2.3排除抢购上限的用户
        Long size = redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX + id).size();
        if(size>=seckillGoods.getStockCount()){
            throw new RuntimeException("商品抢购人数已达到上限");
        }

        //2.4排除用户正在排队的情况
        Boolean aBoolean = redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);
        if(aBoolean){
            throw new RuntimeException("您正在排队...");
        }

        //用户排队  表示用户进入排队中（要创建订单的）
        RecordInfo info = new RecordInfo();
        info.setId(id);
        info.setUserId(userId);
        redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(info);

        //设置用户在排队的标识
        redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId,id);

        //限制商品抢购上限,压入队列（若只有5个商品，已经优5人在排队了，第6人就不能让他排队了）
        redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX+id).leftPush(id);

        //多线程创建订单（从用户排队队列中获取）
        //new Thread(createOrderHandlerThread).start();
        threadPoolTaskExecutor.execute(createOrderHandlerThread);


        /*//3.减库存,再存储到redis中
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        redisTemplate.boundHashOps("seckillGoods").put(id,seckillGoods);

        //4.若秒杀商品为0，删除redis中商品，更新数据库
        if(seckillGoods.getStockCount()<=0){
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            redisTemplate.boundHashOps("seckillGoods").delete(id);
        }

        //5.下预订单在redis中
        TbSeckillOrder order = new TbSeckillOrder();

        order.setId(new IdWorker(1,1).nextId());
        order.setSeckillId(id);
        order.setMoney(seckillGoods.getCostPrice());
        order.setSellerId(seckillGoods.getSellerId());
        order.setCreateTime(new Date());
        order.setStatus("0");

        redisTemplate.boundHashOps("seckillOrder").put(userId,order);*/

    }

    @Override
    public TbSeckillOrder getFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public Boolean isFlag(String userId) {
        return redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);
    }
}
