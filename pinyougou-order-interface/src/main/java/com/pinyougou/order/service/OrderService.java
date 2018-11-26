package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.order.service
 * @since 1.0
 */
public interface OrderService {

    /**
     * 创建订单
     * @param order
     */
    public void add(TbOrder order);

    public TbPayLog getTbPayLogFromRedis(String userId);

    public void updateTbPayLogAndOrder(String out_trade_no,String transation_id);

}
