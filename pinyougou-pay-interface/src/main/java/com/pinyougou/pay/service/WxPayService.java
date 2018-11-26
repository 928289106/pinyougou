package com.pinyougou.pay.service;

import java.util.Map;

public interface WxPayService {

    /**
     *
     * @param out_trade_no  订单
     * @param tatol_fee     金额
     * @return
     */
    public Map createNative(String out_trade_no,String tatol_fee);

    public Map queryStatus(String out_trade_no);

}
