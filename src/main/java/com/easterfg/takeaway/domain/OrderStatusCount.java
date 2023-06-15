package com.easterfg.takeaway.domain;

import lombok.Data;

/**
 * @author EasterFG on 2023/6/16
 */
@Data
public class OrderStatusCount {

    private int confirm;

    private int prepare;

    private int proceed;

}
