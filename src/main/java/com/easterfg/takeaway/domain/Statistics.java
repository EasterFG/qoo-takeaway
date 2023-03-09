package com.easterfg.takeaway.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Statistics {
    /**
     *
     */
    private Long id;

    /**
     * 营业额
     */
    private BigDecimal turnover;

    /**
     * 总订单
     */
    private Integer totalOrder;

    /**
     * 完成订单
     */
    private Integer completeOrder;

    /**
     * 取消订单
     */
    private Integer cancelOrder;

    /**
     * 用户数
     */
    private Integer userCount;

    /**
     * 记录时间
     */
    private LocalDate createTime;
}

