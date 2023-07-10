package com.easterfg.takeaway.domain;

import lombok.Data;

import java.util.List;

/**
 * @author EasterFG on 2023/6/16
 */
@Data
public class OrderStatusCount {

    private int accept;

    private int delivery;

    private int delivering;

    private int finish;

    private int cancel;

    private int total;

    public OrderStatusCount() {
    }

    public static OrderStatusCount total(List<StatusStatistics> statistics) {
        OrderStatusCount orderStatusCount = new OrderStatusCount();
        for (StatusStatistics statistic : statistics) {
            switch (statistic.getStatus()) {
                case WAIT_ACCEPT -> orderStatusCount.setAccept(statistic.getCount());
                case WAIT_DELIVERY -> orderStatusCount.setDelivery(statistic.getCount());
                case DELIVERING -> orderStatusCount.setDelivering(statistic.getCount());
                case FINISHED -> orderStatusCount.setFinish(statistic.getCount());
                case CANCELLED -> orderStatusCount.setCancel(statistic.getCount());
            }
            orderStatusCount.setTotal(orderStatusCount.getTotal() + statistic.getCount());
        }
        return orderStatusCount;
    }

}

