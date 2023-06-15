package com.easterfg.takeaway.dto;

import lombok.Data;

/**
 * @author EasterFG on 2023/6/12
 */
@Data
public class OrderStatusStatisticsDTO {

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
