package com.easterfg.takeaway.domain;

import lombok.Data;

/**
 * @author EasterFG on 2022/11/15
 * <p>
 * 订单状态统计, 待处理, 待配送, 配送中
 */
@Data
public class OrderStatusStatistics {

    /**
     * 总数
     */
    private int total;

    /**
     * 待确认
     */
    private int confirm;

    /**
     * 待配送
     */
    private int prepare;

    /**
     * 配送中
     */
    private int proceed;

    /**
     * 已完成
     */
    private int complete;

    /**
     * 已取消
     */
    private int cancel;
}
