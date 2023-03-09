package com.easterfg.takeaway.domain;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class OrderOperate {

    private Long id;

    /**
     * 订单编号
     */
    private Long tradeNo;

    /**
     * 操作用户Id
     */
    private Long operatorId;

    /**
     * 操作用户名称
     */
    private String operatorName;

    /**
     * 订单操作后状态
     */
    private Integer status;

    /**
     * 操作简易描述
     */
    private String message;

    /**
     * 日志记录时间
     */
    private LocalDateTime createTime;
}

