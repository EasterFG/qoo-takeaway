package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.AddressBookDAO;
import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.dao.OrderDetailDAO;
import com.easterfg.takeaway.dao.ShoppingCartDAO;
import com.easterfg.takeaway.domain.*;
import com.easterfg.takeaway.dto.*;
import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.exception.AccessDeniedException;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.machine.MachineUtils;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.OrderService;
import com.easterfg.takeaway.service.PayService;
import com.easterfg.takeaway.utils.SnowflakeIdWorker;
import com.easterfg.takeaway.utils.constant.GlobalConstant;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/10/24
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_EXISTS = "订单不存在";

    /**
     * 存储订单超时主动取消信息, 需要保证线程安全
     */
    private final ConcurrentHashMap<Long, Timeout> orderTimeout = new ConcurrentHashMap<>();

    @Resource
    private StringRedisTemplate template;

    @Resource
    private AddressBookDAO addressBookDAO;

    @Resource
    private ShoppingCartDAO shoppingCartDAO;

    @Resource
    private OrderDetailDAO orderDetailDAO;

    @Resource
    private OrderDAO orderDAO;

    @Resource(name = "alipayMobileServiceImpl")
    private PayService payService;

    @Resource
    private HashedWheelTimer hashedWheelTimer;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private MachineUtils machineUtils;

    @Override
    @Transactional
    public Long create(OrderDTO orderDTO) {
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
//        AddressBook addressBook = addressBookService.getAddress(orderDTO.getAddressBookId());
        AddressBook addressBook = addressBookDAO.selectById(orderDTO.getAddressBookId(), user.getId());
        if (addressBook == null) {
            throw new BusinessException("40001", "配送地址为空");
        }
        // 创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTradeNo(tradeNo);
        order.setPayMethod(orderDTO.getPayMethod());
        order.setAmount(total);
        order.setRemark(orderDTO.getRemark());
        order.setAddress(addressBook.getCity() + addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        orderDAO.createOrder(order);
        // 需要事务保证全部数据插入成功
        for (ShoppingCart cart : carts) {
            OrderDetail od = new OrderDetail();
            od.setName(cart.getName());
            od.setImage(cart.getImage());
            od.setTradeNo(order.getTradeNo());
            od.setDishId(cart.getDishId());
            od.setComboId(cart.getComboId());
            od.setDishFlavor(cart.getDishFlavor());
            od.setNumber(cart.getNumber());
            od.setAmount(cart.getAmount());
            orderDetailDAO.insert(od);
        }
        // 清空购物车数据
        shoppingCartDAO.deleteByUserId(user.getId());
        Timeout tm = hashedWheelTimer.newTimeout(timeout -> cancel(tradeNo, null, "订单超时自动取消"), 15, TimeUnit.MINUTES);
        orderTimeout.put(tradeNo, tm);
        // 返回订单编号
        return tradeNo;
    }

    /**
     * 订单支付
     *
     * @param tradeNo 订单编号
     */
    @Override
    public String pay(Long tradeNo) {
        // 查询订单信息
        Long userId = UserContext.getUserId();
        Order order = orderDAO.getOrderStatus(tradeNo, userId);
        if (order == null) {
            throw new AccessDeniedException("你无法支付其他人的订单");
        }
        // 订单状态 待支付
        if (order.getStatus() == OrderStatus.WAIT_PAYMENT) {
            // 查询订单信息, 如果订单信息存在
//            if (order.getOutTradeNo() != null) {
            PayQueryDTO query = payService.queryOrder(tradeNo);
            if (query != null && query.getTradeStatus() == PayQueryDTO.Status.TRADE_SUCCESS) {
                // 订单已经支付, 通知状态机更新
                machineUtils.sendMessage(OrderEvent.PAYMENT, order);
                // 更新订单信息
                Order o = new Order();
                o.setTradeNo(tradeNo);
                o.setOutTradeNo(query.getOutTradeNo());
                o.setPaymentTime(query.getPaymentTime());
                orderDAO.updateOrder(order);
                throw new BusinessException("订单已经支付");
            }
//            }
            // 调用三方支付接口
            String api;
            try {
                LocalDateTime timeout = order.getCreateTime().plusMinutes(15);
                api = payService.payOrder(tradeNo, order.getAmount(),
                        GlobalConstant.DEFAULT_FORMATTER.format(timeout));
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("20001", "第三方服务调用失败");
            }
            // 返回支付表单
            return api;
        } else {
            // 如果订单不是待支付
            throw new BusinessException("订单无需支付");
        }
    }

    @Override
    public void accept(Long tradeNo) {
        Order order = new Order();
        order.setTradeNo(tradeNo);
        // 通知状态更新
        machineUtils.sendMessage(OrderEvent.ACCEPT, order);
    }

    @Override
    public void startDelivery(Long tradeNo) {
        Order order = new Order();
        order.setTradeNo(tradeNo);
        machineUtils.sendMessage(OrderEvent.START_DELIVERY, order);
    }

    @Override
    public void finished(long tradeNo) {
        Order order = new Order();
        order.setTradeNo(tradeNo);
        order.setCloseTime(LocalDateTime.now());
//        orderDAO.updateOrder(order, null);
        machineUtils.sendMessage(OrderEvent.FINISH, order);
    }

    @Override
    public void refund(RefundDTO refundDTO) {
        Order order = orderDAO.getOrder(refundDTO.getTradeNo());
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }

        switch (order.getStatus()) {
            case WAIT_PAYMENT -> throw new BusinessException("未付款的订单无法退款");
            case REFUNDING -> throw new BusinessException("订单正在退款中");
            case REFUNDED -> throw new BusinessException("订单无法退款");
        }

        if (order.getAmount().compareTo(BigDecimal.valueOf(refundDTO.getAmount())) < 0) {
            throw new BusinessException("订单退款金额不能大于订单总金额");
        }

        // 通知第三方服务退款
        payService.refund(order.getTradeNo(), order.getOutTradeNo(), refundDTO.getAmount());
    }


    /**
     * 取消订单
     *
     * @param tradeNo      订单编号
     * @param source       取消来源(用户/管理员)
     * @param cancelReason 取消原因
     */
    @Override
    public Result cancel(Long tradeNo, Role source, String cancelReason) {
        UserContext.User user = UserContext.getUser();
        Long userId = null;
        if (source == Role.USER) {
            // 来源用户, 设置uid
            userId = user.getId();
        }
        Order order = orderDAO.getOrderStatus(tradeNo, userId);
        if (order == null) {
            // 订单不存在/权限不足
            throw new BusinessException("订单不存在");
        }
        // 更新订单状态
        Order update = new Order();
        update.setTradeNo(tradeNo);
        update.setCancelReason(cancelReason);
        update.setCloseTime(LocalDateTime.now());
        // 设置状态, 用于状态机更新状态持久化
        update.setStatus(order.getStatus());
        orderDAO.updateOrder(update);

        // 尝试取消订单超时队列
        Optional.ofNullable(orderTimeout.remove(tradeNo)).ifPresent(Timeout::cancel);

        switch (order.getStatus()) {
            case WAIT_PAYMENT -> {
                machineUtils.sendMessage(OrderEvent.CANCEL, update);
                // 通知支付服务取消订单
                payService.cancel(tradeNo);
            }
            case WAIT_ACCEPT -> machineUtils.sendMessage(OrderEvent.REFUNDING, update);
            default -> {
                if (source == Role.USER) {
                    throw new BusinessException("订单取消失败, 请联系商家处理");
                } else {
                    // 通知状态更新为 取消(已退款)
                    machineUtils.sendMessage(OrderEvent.REFUNDING, update);
                }
            }
        }

        // 订单状态为退款中, 通知第三方接口退款
        if (order.getStatus() == OrderStatus.REFUNDING) {
            payService.refund(order.getTradeNo(), String.valueOf(order.getOutTradeNo()), order.getAmount().doubleValue());
            return Result.success("订单已取消, 退款请求正在处理");
        }
        return Result.success("订单已取消");
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
        if (order.getStatus() == OrderStatus.WAIT_PAYMENT) {
            // 主动查询订单信息
            PayQueryDTO query = payService.queryOrder(tradeNo);
            log.info("订单信息查询 tid: {}, status: {}", tradeNo, query);
            // 处理订单状态变更
            if (query != null) {
                PayQueryDTO.Status tradeStatus = query.getTradeStatus();
                switch (tradeStatus) {
                    case TRADE_CLOSED ->
                        // 订单被取消
                            machineUtils.sendMessage(OrderEvent.CANCEL, order);
                    case TRADE_SUCCESS -> {
                        // 订单交易成功
                        Order update = new Order();
                        update.setTradeNo(tradeNo);
                        update.setOutTradeNo(query.getOutTradeNo());
                        update.setPaymentTime(query.getPaymentTime());
                        // 更新订单信息
                        orderDAO.updateOrder(order);
                        // 通知状态机更新
                        machineUtils.sendMessage(OrderEvent.PAYMENT, order);
                    }
                }
            }
        }
        return order;
    }

    @Override
    public OrderStatusCount orderCount() {
        return OrderStatusCount.total(orderDAO.statisticsByDate(LocalDate.now()));
    }

    @Override
    public PageData<Order> listOrder(Long uid, PageQuery pageQuery, OrderStatus status, String tradeNO, String phone, LocalDate start, LocalDate end) {
        if (uid != null) {
            // 手动分页
            int total = orderDAO.count(uid);
            pageQuery.check(total);
            List<Order> orders = orderDAO.listUserOrder(uid,
                    (pageQuery.getPage() - 1) * pageQuery.getPageSize(), pageQuery.getPageSize());
            PageInfo<Order> info = new PageInfo<>(orders);
            return new PageData<>(info);
        } else {
            // 手动分页
            // 验证日期是否合法
            if (start != null && end != null && end.isBefore(start)) {
                throw new BusinessException("结束日期不能早于开始日期");
            }
            int total = orderDAO.countByWhere(status, tradeNO, phone, start, end);
            pageQuery.check(total);
            List<Order> orders = orderDAO.listOrder(status, tradeNO, phone, start, end,
                    (pageQuery.getPage() - 1) * pageQuery.getPageSize(), pageQuery.getPageSize());
            orders.forEach(order -> order.setOrderDetails(orderDetailDAO.listOrderDetail(order.getTradeNo())));
            return new PageData<>(total, orders);
        }
    }
}
