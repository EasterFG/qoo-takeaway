package com.easterfg.takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easterfg.takeaway.dao.OrderDetailDAO;
import com.easterfg.takeaway.domain.OrderDetail;
import com.easterfg.takeaway.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author EasterFG on 2022/10/24
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailDAO, OrderDetail> implements OrderDetailService {
}
