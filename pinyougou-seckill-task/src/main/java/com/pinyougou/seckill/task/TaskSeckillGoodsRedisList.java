package com.pinyougou.seckill.task;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TaskSeckillGoodsRedisList {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //每隔30秒查询一次数据库（将数据库数据更新到redis中）
    @Scheduled(cron = "0/10 * * * * ? ")
    public void pushGoods() {

        //1.从数据库查询秒杀商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);

        Date date = new Date();

        criteria.andStartTimeLessThan(date);                //开始时间小于当前时间
        criteria.andEndTimeGreaterThanOrEqualTo(date);      //结束时间大于等于当前时间

        //排除redis中已有的商品
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        if (seckillGoods != null && seckillGoods.size() > 0) {
            List<Long> values = new ArrayList<>();
            for (TbSeckillGoods seckillGood : seckillGoods) {
                values.add(seckillGood.getId());
            }
            criteria.andIdNotIn(values);
        }
        List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
        System.out.println(tbSeckillGoods.size());

        //2.将商品数据放到数据库中
        if (tbSeckillGoods != null) {
            for (TbSeckillGoods seckillGood : tbSeckillGoods) {

                //将商品压入队列中，一个商品就是一个队列
                pushQueueSeckillGoods(seckillGood);

                //将数据库数据存入redis中
                redisTemplate.boundHashOps("seckillGoods").put(seckillGood.getId(), seckillGood);
            }
        }
    }

    public void pushQueueSeckillGoods(TbSeckillGoods seckillGood){
        for (int i = 0; i < seckillGood.getStockCount(); i++) {
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+seckillGood.getId()).leftPush(seckillGood.getId());

        }
    }

}
