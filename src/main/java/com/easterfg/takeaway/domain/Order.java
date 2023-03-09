package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@TableName("takeout_order")
public class Order {
    /**
     * 自增主键
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "自增主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 下单用户ID
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "下单用户ID")
    private Long userId;

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private Long tradeNo;

    /**
     * 交易流水号
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "交易流水号")
    private String outTradeNo;

    /**
     * 订单状态 1.待付款 2.待派送 3.已派送 4.已完成 5.已取消
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "订单状态 1.待付款 2.待接单 3.待派送 4.派送中 5.已完成 6.已取消")
    private Integer status;

    /**
     * 支付渠道 1.微信 2.支付宝
     */
    @ApiModelProperty(value = "支付渠道 1.微信 2.支付宝")
    private Integer payMethod;

    /**
     * 支付状态 0:未支付 1: 已支付 2: 已退款
     */
    @ApiModelProperty(value = "支付状态 0:未支付 1: 已支付 2: 已退款")
    private Integer payStatus;

    /**
     * 订单总金额
     */
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal amount;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 取消原因
     */
    @ApiModelProperty(value = "取消原因")
    private String cancelReason;

    /**
     * 下单地址
     */
    @ApiModelProperty(value = "下单地址")
    private String address;

    /**
     * 收货人
     */
    @ApiModelProperty(value = "收货人")
    private String consignee;

    /**
     * 联系方式
     */
    @ApiModelProperty(value = "联系方式")
    private String phone;

    /**
     * 创建时间(下单时间)
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间(下单时间)")
    private LocalDateTime createTime;

    /**
     * 付款时间
     */
    @ApiModelProperty(value = "付款时间")
    private LocalDateTime paymentTime;

    /**
     * 完成/取消时间
     */
    @ApiModelProperty(value = "完成/取消时间")
    private LocalDateTime closeTime;


    @TableField(exist = false)
    private List<OrderDetail> orderDetails;
}
