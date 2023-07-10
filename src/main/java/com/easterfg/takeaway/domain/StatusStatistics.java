package com.easterfg.takeaway.domain;

import com.easterfg.takeaway.enums.OrderStatus;
import lombok.Data;

/**
 * @author EasterFG on 2022/11/15
 * <p>
 * 订单状态统计, 待处理, 待配送, 配送中
 */
@Data
public class StatusStatistics {

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 总数
     */
    private int count;
}
