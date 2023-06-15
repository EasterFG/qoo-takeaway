package com.easterfg.takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easterfg.takeaway.dao.AddressBookDAO;
import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.dao.OrderDetailDAO;
import com.easterfg.takeaway.dao.ShoppingCartDAO;
import com.easterfg.takeaway.domain.*;
import com.easterfg.takeaway.dto.OrdersDTO;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.dto.PayQueryDTO;
import com.easterfg.takeaway.exception.AccessDeniedException;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.OrderDetailService;
import com.easterfg.takeaway.service.OrderOperateService;
import com.easterfg.takeaway.service.OrderService;
import com.easterfg.takeaway.service.PayService;
import com.easterfg.takeaway.utils.MapUtils;
import com.easterfg.takeaway.utils.OrderLogRecord;
import com.easterfg.takeaway.utils.SnowflakeIdWorker;
import com.easterfg.takeaway.utils.constant.GlobalConstant;
import com.easterfg.takeaway.utils.enums.OrderStatus;
import com.easterfg.takeaway.utils.enums.PayStatus;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/10/24
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDAO, Order> implements OrderService {

    private static final String ORDER_NOT_EXISTS = "订单不存在";
    /**
     * 存储订单超时主动取消信息, 需要保证线程安全
     */
    private final ConcurrentHashMap<Long, Timeout> orderTimeout = new ConcurrentHashMap<>();
    @Resource

    private StringRedisTemplate template;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Resource
    private AddressBookDAO addressBookDAO;

    @Resource
    private ShoppingCartDAO shoppingCartDAO;

    @Resource
    private OrderDetailDAO orderDetailDAO;

    @Resource
    private OrderDetailService orderDetailService;

    @Resource
    private OrderDAO orderDAO;

    @Resource(name = "alipayMobileServiceImpl")
    private PayService payService;

    @Resource
    private HashedWheelTimer hashedWheelTimer;

    @Resource
    private OrderOperateService orderOperateService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @OrderLogRecord(value = "用户提交订单",
            status = GlobalConstant.WAIT_PAYMENT)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> placeAnOrder(OrdersDTO ordersDTO) {
        UserContext.User user = UserContext.getUser();
        // 生成订单编号
        long tradeNo = snowflakeIdWorker.nextId();
        // 将订单set进redis
        String key = GlobalConstant.TRADE_KEY + user.getId();
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            throw new BusinessException("服务器繁忙, 请稍后再试");
        }
        template.opsForValue().set(key, String.valueOf(tradeNo), 30, TimeUnit.SECONDS);
        // 通过购物车数据
//        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class);
//        wrapper.eq(ShoppingCart::getUserId, user.getId());
        List<ShoppingCart> carts = shoppingCartDAO.listByUserId(user.getId());
        if (carts.isEmpty()) {
            throw new BusinessException("40001", "订单商品为空");
        }
        // 最低付款1分
        BigDecimal total = carts.stream()
                .map(c -> {
                    BigDecimal number = BigDecimal.valueOf(c.getNumber());
                    // 单价 * 数量 & 打包费1r / 件商品
                    return c.getAmount().multiply(number).add(number);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 配送费6r
        total = total.add(BigDecimal.valueOf(6));
        // 查询地址
//        AddressBook addressBook = addressBookService.getAddress(ordersDTO.getAddressBookId());
        AddressBook addressBook = addressBookDAO.selectById(ordersDTO.getAddressBookId(), user.getId());
        if (addressBook == null) {
            throw new BusinessException("40001", "配送地址为空");
        }
        // 创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTradeNo(tradeNo);
        order.setPayMethod(ordersDTO.getPayMethod());
        order.setAmount(total);
        order.setRemark(order.getRemark());
        order.setAddress(addressBook.getCity() + addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        // 需要事务保证全部数据插入成功
        orderDAO.insertOrder(order);
//        save(order);
        // 保存购物车数据
        int count = 0;
        try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
            OrderDetailDAO dao = sqlSession.getMapper(OrderDetailDAO.class);
            for (ShoppingCart cart : carts) {
                OrderDetail od = new OrderDetail();
                od.setName(cart.getName());
                od.setImage(cart.getImage());
                od.setTradeNo(order.getTradeNo());
//                od.setOrderId(order.getId());
                od.setDishId(cart.getDishId());
                od.setComboId(cart.getComboId());
                od.setDishFlavor(cart.getDishFlavor());
                od.setNumber(cart.getNumber());
                od.setAmount(cart.getAmount());
//                od.setOrderId(order.getId());
                count += dao.insert(od);
            }
            sqlSession.commit();
        }
        if (count < carts.size()) {
            throw new BusinessException("20000", "服务器繁忙,请稍后再试");
        }
//        AtomicInteger count = new AtomicInteger();
//        carts.forEach(item -> {
//            OrderDetail detail = new OrderDetail();
//            BeanUtils.copyProperties(item, detail);
//            detail.setOrderId(order.getId());
//            detail.setId(null);
//            // 保存
//            count.addAndGet(this.orderDetailDAO.insert(detail));
//        });
//        if (count.get() < carts.size()) {
//            throw new BusinessException("20000", "订单创建失败");
//        }
        // 订单创建成功, 通知用户支付
        String form;
        try {
            LocalDateTime timeout = order.getCreateTime().plusMinutes(15);
            form = payService.payOrder(tradeNo, total,
                    GlobalConstant.DEFAULT_FORMATTER.format(timeout));
        } catch (Exception e) {
            throw new BusinessException("20000", "第三方服务调用失败");
        }

        // 清空购物车数据
//        shoppingCartService.clean(user.getId());
        shoppingCartDAO.deleteByUserId(user.getId());
        // 超时检查
        Timeout tm = hashedWheelTimer.newTimeout(timeout -> cancelOrder(tradeNo, null, "订单超时自动取消"), 15, TimeUnit.MINUTES);
        orderTimeout.put(tradeNo, tm);
        // 记录日志
        orderOperateService.recordLog(user, tradeNo, "用户提交订单", GlobalConstant.WAIT_PAYMENT);
        // 调用支付
        return MapUtils.hashMap().put("tradeNo", tradeNo).put("form", form).build();
    }

    @Override
    public Order getOrder(long tradeNo) {
        Order order = orderDAO.getOrder(tradeNo);
        UserContext.User user = UserContext.getUser();
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }
        if (user.hasRole(Role.USER) && !order.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("服务器繁忙, 请稍后再试");
        }
        // 如果订单未支付, 调用支付接口查询信息
        if (order.getPayStatus() == PayStatus.UNPAID && order.getStatus() == OrderStatus.WAIT_PAYMENT) {
            // 主动查询订单信息
            PayQueryDTO query = payService.queryOrder(tradeNo);
            log.info("订单信息查询 tid: {}, status: {}", tradeNo, query);
            if (query != null && query.getTradeStatus().equals(PayQueryDTO.Status.TRADE_SUCCESS)) {
                // 更新用户支付状态
//                Order o1 = new Order();
//                o1.setTradeNo(tradeNo);
//                o1.setStatus(GlobalConstant.WAIT_APPROVAL);
                order.setTradeNo(tradeNo);
                order.setOutTradeNo(query.getOutTradeNo());
                order.setStatus(OrderStatus.WAIT_ACCEPT);
                order.setPayStatus(PayStatus.PAID);
                order.setPaymentTime(query.getPaymentTime());
//                o1.setPayStatus(1);
//                o1.setPaymentTime(query.getPaymentTime());
                // 把 WAIT_PAYMENT 修改成 WAIT_ACCEPT
//                orderDAO.updateOrderStatus(tradeNo, OrderStatus.WAIT_ACCEPT, OrderStatus.WAIT_PAYMENT);
//                orderDAO.updateStatus(order, 1);
                orderDAO.updateOrder2(order, OrderStatus.WAIT_PAYMENT);
                // 修改已经获取的订单状态
                orderOperateService.recordLog(user, tradeNo, "用户支付订单",
                        GlobalConstant.WAIT_APPROVAL);
                // 交易成功, 忽略出现的业务异常, 打印日志
//                try {
//                    userPay(tradeNo, query.getPaymentTime());
//                    // 手动记录日志
//                    OrderOperate entity = new OrderOperate();
//                    entity.setTradeNo(tradeNo);
//                    entity.setOperatorId(user.getId());
//                    entity.setOperatorName(user.getName());
//                    entity.setMessage("用户支付");
//                    entity.setStatus(GlobalConstant.WAIT_APPROVAL);
//                    entity.setCreateTime(LocalDateTime.now());
//                    orderOperateService.save(entity);
//                } catch (BusinessException e) {
//                    log.error("查询业务出现异常", e);
//                }
            }
        }
        return order;
    }

//    @OrderLogRecord(value = "用户支付订单", status = GlobalConstant.WAIT_APPROVAL)
//    @Override
//    public boolean userPay(long tradeNo, LocalDateTime paymentTime) {
//        Order order = orderDAO.getOrder(tradeNo);
//        if (order == null) {
//            throw new BusinessException(ORDER_NOT_EXISTS);
//        }
//        if (order.getPayStatus() != 0 || order.getStatus() != GlobalConstant.WAIT_PAYMENT) {
//            throw new BusinessException("订单不能支付");
//        }
//        LambdaUpdateWrapper<Order> wrapper = Wrappers.lambdaUpdate(Order.class);
//        wrapper.eq(Order::getTradeNo, tradeNo)
//                .set(Order::getPayStatus, 1)
//                .set(Order::getPaymentTime, paymentTime)
//                .set(Order::getStatus, GlobalConstant.WAIT_APPROVAL);
//
//        return orderDAO.update(null, wrapper) > 1;
//    }

    @Override
    public OrderStatusCount orderCount() {
        return orderDAO.statistics();
    }

    @OrderLogRecord(value = "订单被取消",
            status = GlobalConstant.CANCEL)
    @Override
    public void cancelOrder(Long tradeNo, Long uid, String cancelReason) {
        // 查询订单状态
        Order order = orderDAO.getOrderStatus(tradeNo, uid);
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }
        // 用户允许在 等待(2) 时取消, 其他状态下联系商家取消
        // 商家仅允许在 完成/取消状态前取消
        // 只允许在等待商家接单时允许取消
        if (uid == null) {
            // 验证订单是否属于当前用户
            if (order.getStatus().getCode() > 4) {
                throw new BusinessException("订单不允许取消");
            }
        } else {
            // 预期为 2
            if (order.getStatus() != OrderStatus.WAIT_PAYMENT &&
                    order.getStatus() != OrderStatus.WAIT_ACCEPT) {
                throw new BusinessException("订单取消失败， 请联系商家处理");
            }
        }
        // 更新状态
        Order o1 = new Order();
        o1.setTradeNo(tradeNo);
        o1.setStatus(OrderStatus.CANCELLED);
        o1.setCloseTime(LocalDateTime.now());
        o1.setCancelReason(cancelReason);
        // 移除对应定时任务
        Optional.ofNullable(orderTimeout.remove(tradeNo)).ifPresent(Timeout::cancel);
        // 修改数据库数据
//        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate(Order.class)
//                .set(Order::getStatus, GlobalConstant.CANCEL)
//                .set(Order::getCloseTime, LocalDateTime.now())
//                .set(Order::getCancelReason, cancelReason)
//                .eq(Order::getTradeNo, tradeNo);
        // 订单未支付, 通知支付平台取消订单
        if (order.getPayStatus() == PayStatus.UNPAID) {
            payService.cancel(tradeNo);
        } else if (order.getPayStatus() == PayStatus.PAID && order.getStatus() == OrderStatus.WAIT_ACCEPT) {
            // 通过支付宝流水号退款
            // 订单已经支付 等待商家接单时， 通知支付宝退款, 异步修改订单支付状态
            payService.refund(order.getOutTradeNo(), order.getAmount().doubleValue());
            // 设置支付状态
        }
        orderDAO.updateOrder(o1);
        // 通过预期值来确保幂等
//        orderDAO.updateStatus(o1, order.getStatus().getCode());
    }

//    @OrderLogRecord(value = "商家接单",
//            status = GlobalConstant.WAIT_DELIVERY)
//    @Override
//    public boolean approveOrder(Long tradeNo) {
//        Order order = orderDAO.getOrderStatus(tradeNo, null);
//        if (order.getStatus() != GlobalConstant.WAIT_APPROVAL) {
//            throw new BusinessException(GlobalConstant.ORDER_STATUS_EXCEPTION);
//        }
//        // 修改订单数据
//        order.setStatus(GlobalConstant.WAIT_DELIVERY);
//        return orderDAO.updateStatus(order) > 0;
//    }
//
//    @OrderLogRecord(value = "订单开始配送", status = GlobalConstant.DISTRIBUTION)
//    @Override
//    public boolean deliveryOrder(Long tradeNo) {
//        Order order = orderDAO.getOrderStatus(tradeNo, null);
//        if (order.getStatus() != GlobalConstant.WAIT_DELIVERY) {
//            throw new BusinessException(GlobalConstant.ORDER_STATUS_EXCEPTION);
//        }
//        order.setStatus(GlobalConstant.DISTRIBUTION);
//        return orderDAO.updateStatus(order) > 0;
//    }
//
//    @OrderLogRecord(value = "订单配送完成", status = GlobalConstant.FINISH)
//    @Override
//    public boolean completeOrder(Long tradeNo) {
//        Order order = orderDAO.getOrderStatus(tradeNo, null);
//        if (order.getStatus() != GlobalConstant.DISTRIBUTION) {
//            throw new BusinessException(GlobalConstant.ORDER_STATUS_EXCEPTION);
//        }
//        order.setStatus(GlobalConstant.FINISH);
//        return orderDAO.updateStatus(order) > 0;
//    }

    @Transactional
    @Override
    public boolean updateOrderStatus(long tradeNo, OrderStatus expected, OrderStatus actual) {
        // 接单 配送 完成 状态更新
        Order order = new Order();
        order.setStatus(actual);
        order.setTradeNo(tradeNo);
        // 如果是完成状态, 就设置取消时间
        if (actual == OrderStatus.FINISHED) {
            order.setCloseTime(LocalDateTime.now());
        }
        // 更新订单状态
        int row = orderDAO.updateOrder2(order, expected);
        if (row > 0) {
            // 记录日志
            UserContext.User user = UserContext.getUser();
            String message;
            switch (actual) {
                case WAIT_DELIVERY:
                    message = "商家接单";
                    break;
                case DELIVERING:
                    message = "订单开始配送";
                    break;
                case FINISHED:
                    message = "订单配送完成";
                    break;
                default:
                    message = "未知操作";
            }
            orderOperateService.recordLog(user, tradeNo, message, actual.getCode());
            return true;
        }
        return false;
    }

    @OrderLogRecord(value = "订单退款", status = GlobalConstant.CANCEL)
    @Override
    public void refundOrder(long tradeNo, double amount) {
        Order order = getBaseMapper().getOrder(tradeNo);
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }
        if (order.getPayStatus() != PayStatus.PAID) {
            // 订单不能退款
            throw new BusinessException("订单不能退款");
        }
        if (amount > order.getAmount().doubleValue()) {
            throw new BusinessException("订单退款金额不能大于订单总金额");
        }
        UserContext.User user = UserContext.getUser();
        try {
            payService.refund(order.getOutTradeNo(), amount).get();
            orderOperateService.recordLog(user, tradeNo, "订单退款", GlobalConstant.CANCEL);
        } catch (ExecutionException e) {
            log.error("订单退款异常");
            throw new BusinessException("第三方服务调用失败");
        } catch (InterruptedException e) {
            log.info("退款操作被打断");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public PageData<Order> listOrder(PageQuery pageQuery, OrderStatus status, Long uid) {
        if (uid != null) {
            PageMethod.startPage(pageQuery.getPage(), pageQuery.getPageSize());
            List<Order> orders = orderDAO.listUserOrder(uid);
            PageInfo<Order> info = new PageInfo<>(orders);
            return new PageData<>(info);
//            int total = orderDAO.count(uid);
//            if (total == 0) {
//                pageData.setTotal(0);
//                return pageData;
//            }
//            int max = total / pageQuery.getPageSize() + 1;
//            if (pageQuery.getPage() > max) {
//                pageQuery.setPage(max);
//            }
//            int offset = (pageQuery.getPage() - 1) * pageQuery.getPageSize();
//            List<Order> records = orderDAO.listUserOrder(uid);
//            pageData.setTotal(total);
//            pageData.setRecords(records);
        } else {
            // 查询全部数据
//            PageMethod.startPage(pageQuery.getPage(), pageQuery.getPageSize());
            // 手动分页
            int total = orderDAO.countByStatus(status);
            pageQuery.check(total);
            List<Order> orders = orderDAO.listOrder(status,
                    (pageQuery.getPage() - 1) * pageQuery.getPageSize(), pageQuery.getPageSize());
//            PageInfo<Order> info = new PageInfo<>(orders);
            orders.forEach(order -> order.setOrderDetails(orderDetailDAO.listOrderDetail(order.getTradeNo())));
            return new PageData<>(total, orders);
//            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>(Order.class)
//                    .eq(status != null, Order::getStatus, status);
//            Page<Order> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
//            this.page(page, wrapper);
//            orderDAO.listOrder(status);
//            // 查询订单详情
//            page.getRecords().forEach(order -> {
//                // 仅需要名称和数量
//                LambdaQueryWrapper<OrderDetail> query = Wrappers.lambdaQuery(OrderDetail.class)
//                        .select(OrderDetail::getName, OrderDetail::getNumber, OrderDetail::getDishFlavor)
//                        .eq(OrderDetail::getOrderId, order.getId());
//                List<OrderDetail> details = orderDetailDAO.selectList(query);
//                order.setOrderDetails(details);
//            });
//            pageData.setTotal(page.getTotal());
//            pageData.setRecords(page.getRecords());
        }
    }
}
