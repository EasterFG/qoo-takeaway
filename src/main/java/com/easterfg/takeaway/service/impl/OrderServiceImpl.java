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

    private static final String ORDER_NOT_EXISTS = "???????????????";
    /**
     * ????????????????????????????????????, ????????????????????????
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

    @OrderLogRecord(value = "??????????????????",
            status = GlobalConstant.WAIT_PAYMENT)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> placeAnOrder(OrdersDTO ordersDTO) {
        UserContext.User user = UserContext.getUser();
        // ??????????????????
        long tradeNo = snowflakeIdWorker.nextId();
        // ?????????set???redis
        String key = GlobalConstant.TRADE_KEY + user.getId();
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            throw new BusinessException("???????????????, ???????????????");
        }
        template.opsForValue().set(key, String.valueOf(tradeNo), 30, TimeUnit.SECONDS);
        // ?????????????????????
//        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class);
//        wrapper.eq(ShoppingCart::getUserId, user.getId());
        List<ShoppingCart> carts = shoppingCartDAO.listByUserId(user.getId());
        if (carts.isEmpty()) {
            throw new BusinessException("40001", "??????????????????");
        }
        // ????????????1???
        BigDecimal total = carts.stream()
                .map(c -> {
                    BigDecimal number = BigDecimal.valueOf(c.getNumber());
                    // ?????? * ?????? & ?????????1r / ?????????
                    return c.getAmount().multiply(number).add(number);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // ?????????6r
        total = total.add(BigDecimal.valueOf(6));
        // ????????????
//        AddressBook addressBook = addressBookService.getAddress(ordersDTO.getAddressBookId());
        AddressBook addressBook = addressBookDAO.selectById(ordersDTO.getAddressBookId(), user.getId());
        if (addressBook == null) {
            throw new BusinessException("40001", "??????????????????");
        }
        // ????????????
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTradeNo(tradeNo);
        order.setPayMethod(ordersDTO.getPayMethod());
        order.setAmount(total);
        order.setRemark(order.getRemark());
        order.setAddress(addressBook.getCity() + addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        // ??????????????????????????????????????????
        orderDAO.insertOrder(order);
//        save(order);
        // ?????????????????????
        int count = 0;
        try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
            OrderDetailDAO dao = sqlSession.getMapper(OrderDetailDAO.class);
            for (ShoppingCart cart : carts) {
                OrderDetail od = new OrderDetail();
                od.setName(cart.getName());
                od.setImage(cart.getImage());
                od.setOrderId(order.getId());
                od.setDishId(cart.getDishId());
                od.setComboId(cart.getComboId());
                od.setDishFlavor(cart.getDishFlavor());
                od.setNumber(cart.getNumber());
                od.setAmount(cart.getAmount());
                od.setOrderId(order.getId());
                count += dao.insert(od);
            }
            sqlSession.commit();
        }
        if (count < carts.size()) {
            throw new BusinessException("20000", "???????????????,???????????????");
        }
//        AtomicInteger count = new AtomicInteger();
//        carts.forEach(item -> {
//            OrderDetail detail = new OrderDetail();
//            BeanUtils.copyProperties(item, detail);
//            detail.setOrderId(order.getId());
//            detail.setId(null);
//            // ??????
//            count.addAndGet(this.orderDetailDAO.insert(detail));
//        });
//        if (count.get() < carts.size()) {
//            throw new BusinessException("20000", "??????????????????");
//        }
        // ??????????????????, ??????????????????
        String form;
        try {
            LocalDateTime timeout = order.getCreateTime().plusMinutes(15);
            form = payService.payOrder(tradeNo, total,
                    GlobalConstant.DEFAULT_FORMATTER.format(timeout));
        } catch (Exception e) {
            throw new BusinessException("20000", "???????????????????????????");
        }

        // ?????????????????????
//        shoppingCartService.clean(user.getId());
        shoppingCartDAO.deleteByUserId(user.getId());
        // ????????????
        Timeout tm = hashedWheelTimer.newTimeout(timeout -> cancelOrder(tradeNo, null, "????????????????????????"), 15, TimeUnit.MINUTES);
        orderTimeout.put(tradeNo, tm);
        // ????????????
        orderOperateService.recordLog(user, tradeNo, "??????????????????", GlobalConstant.WAIT_PAYMENT);
        // ????????????
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
            throw new AccessDeniedException("???????????????, ???????????????");
        }
        // ?????????????????????, ??????????????????????????????
        if (order.getPayStatus() == 0 && order.getStatus() == GlobalConstant.WAIT_PAYMENT) {
            // ????????????????????????
            PayQueryDTO query = payService.queryOrder(tradeNo);
            log.info("?????????????????? tid: {}, status: {}", tradeNo, query);
            if (query != null && query.getTradeStatus().equals(PayQueryDTO.Status.TRADE_SUCCESS)) {
                // ????????????????????????
//                Order o1 = new Order();
//                o1.setTradeNo(tradeNo);
//                o1.setStatus(GlobalConstant.WAIT_APPROVAL);
                order.setTradeNo(tradeNo);
                order.setOutTradeNo(query.getOutTradeNo());
                order.setStatus(GlobalConstant.WAIT_APPROVAL);
                order.setPayStatus(1);
                order.setPaymentTime(query.getPaymentTime());
//                o1.setPayStatus(1);
//                o1.setPaymentTime(query.getPaymentTime());
                orderDAO.updateStatus(order, 1);
                // ?????????????????????????????????
                orderOperateService.recordLog(user, tradeNo, "??????????????????",
                        GlobalConstant.WAIT_APPROVAL);
                // ????????????, ???????????????????????????, ????????????
//                try {
//                    userPay(tradeNo, query.getPaymentTime());
//                    // ??????????????????
//                    OrderOperate entity = new OrderOperate();
//                    entity.setTradeNo(tradeNo);
//                    entity.setOperatorId(user.getId());
//                    entity.setOperatorName(user.getName());
//                    entity.setMessage("????????????");
//                    entity.setStatus(GlobalConstant.WAIT_APPROVAL);
//                    entity.setCreateTime(LocalDateTime.now());
//                    orderOperateService.save(entity);
//                } catch (BusinessException e) {
//                    log.error("????????????????????????", e);
//                }
            }
        }
        return order;
    }

//    @OrderLogRecord(value = "??????????????????", status = GlobalConstant.WAIT_APPROVAL)
//    @Override
//    public boolean userPay(long tradeNo, LocalDateTime paymentTime) {
//        Order order = orderDAO.getOrder(tradeNo);
//        if (order == null) {
//            throw new BusinessException(ORDER_NOT_EXISTS);
//        }
//        if (order.getPayStatus() != 0 || order.getStatus() != GlobalConstant.WAIT_PAYMENT) {
//            throw new BusinessException("??????????????????");
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
    public OrderStatusStatistics statistics() {
        return orderDAO.statistics();
    }

    @OrderLogRecord(value = "???????????????",
            status = GlobalConstant.CANCEL)
    @Override
    public void cancelOrder(Long tradeNo, Long uid, String cancelReason) {
        // ??????????????????
        Order order = orderDAO.getOrderStatus(tradeNo, uid);
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }
        // ??????????????? ??????(2) ?????????, ?????????????????????????????????
        // ?????????????????? ??????/?????????????????????
        // ?????????????????????????????????????????????
        if (uid == null) {
            // ????????????????????????????????????
            if (order.getStatus() > 4) {
                throw new BusinessException("?????????????????????");
            }
        } else {
            // ????????? 2
            if (order.getStatus() != GlobalConstant.WAIT_PAYMENT &&
                    order.getStatus() != GlobalConstant.WAIT_APPROVAL) {
                throw new BusinessException("????????????????????? ?????????????????????");
            }
        }
        // ????????????
        Order o1 = new Order();
        o1.setStatus(GlobalConstant.CANCEL);
        o1.setCloseTime(LocalDateTime.now());
        o1.setCancelReason(cancelReason);
        o1.setTradeNo(tradeNo);
        // ????????????????????????
        Optional.ofNullable(orderTimeout.remove(tradeNo)).ifPresent(Timeout::cancel);
        // ?????????????????????
//        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate(Order.class)
//                .set(Order::getStatus, GlobalConstant.CANCEL)
//                .set(Order::getCloseTime, LocalDateTime.now())
//                .set(Order::getCancelReason, cancelReason)
//                .eq(Order::getTradeNo, tradeNo);
        // ???????????????, ??????????????????????????????
        if (order.getPayStatus() == 0) {
            payService.cancel(tradeNo);
        } else if (order.getPayStatus() == 1 && order.getStatus() == GlobalConstant.WAIT_APPROVAL) {
            // ??????????????????????????????
            // ?????????????????? ???????????????????????? ?????????????????????, ??????????????????????????????
            payService.refund(order.getOutTradeNo(), order.getAmount().doubleValue());
            // ??????????????????
        }
        // ??????????????????????????????
        orderDAO.updateStatus(o1, order.getStatus());
    }

//    @OrderLogRecord(value = "????????????",
//            status = GlobalConstant.WAIT_DELIVERY)
//    @Override
//    public boolean approveOrder(Long tradeNo) {
//        Order order = orderDAO.getOrderStatus(tradeNo, null);
//        if (order.getStatus() != GlobalConstant.WAIT_APPROVAL) {
//            throw new BusinessException(GlobalConstant.ORDER_STATUS_EXCEPTION);
//        }
//        // ??????????????????
//        order.setStatus(GlobalConstant.WAIT_DELIVERY);
//        return orderDAO.updateStatus(order) > 0;
//    }
//
//    @OrderLogRecord(value = "??????????????????", status = GlobalConstant.DISTRIBUTION)
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
//    @OrderLogRecord(value = "??????????????????", status = GlobalConstant.FINISH)
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
    public boolean updateOrderStatus(long tradeNo, int expected, int actual) {
        // ?????? ?????? ?????? ????????????
        Order order = new Order();
        order.setStatus(actual);
        order.setTradeNo(tradeNo);
        // ?????????????????????, ?????????????????????
        if (actual == GlobalConstant.FINISH) {
            order.setCloseTime(LocalDateTime.now());
        }
        // ??????????????????
        int row = orderDAO.updateStatus(order, expected);
        if (row > 0) {
            // ????????????
            UserContext.User user = UserContext.getUser();
            String message;
            switch (actual) {
                case 3:
                    message = "????????????";
                    break;
                case 4:
                    message = "??????????????????";
                    break;
                case 5:
                    message = "??????????????????";
                    break;
                default:
                    message = "????????????";
            }
            orderOperateService.recordLog(user, tradeNo, message, actual);
            return true;
        }
        return false;
    }

    @OrderLogRecord(value = "????????????", status = GlobalConstant.CANCEL)
    @Override
    public void refundOrder(long tradeNo, double amount) {
        Order order = getBaseMapper().getOrder(tradeNo);
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXISTS);
        }
        if (order.getPayStatus() != 1) {
            // ??????????????????
            throw new BusinessException("??????????????????");
        }
        if (amount > order.getAmount().doubleValue()) {
            throw new BusinessException("?????????????????????????????????????????????");
        }
        UserContext.User user = UserContext.getUser();
        try {
            payService.refund(order.getOutTradeNo(), amount).get();
            orderOperateService.recordLog(user, tradeNo, "????????????", GlobalConstant.CANCEL);
        } catch (ExecutionException e) {
            log.error("??????????????????");
            throw new BusinessException("???????????????????????????");
        } catch (InterruptedException e) {
            log.info("?????????????????????");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public PageData<Order> listOrder(PageQuery pageQuery, Integer status, Long uid) {
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
            // ??????????????????
            PageMethod.startPage(pageQuery.getPage(), pageQuery.getPageSize());
            List<Order> orders = orderDAO.listOrder(status);
            PageInfo<Order> info = new PageInfo<>(orders);
            info.getList().forEach(order -> order.setOrderDetails(orderDetailDAO.listOrderDetail(order.getId())));
            return new PageData<>(info);
//            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>(Order.class)
//                    .eq(status != null, Order::getStatus, status);
//            Page<Order> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
//            this.page(page, wrapper);
//            orderDAO.listOrder(status);
//            // ??????????????????
//            page.getRecords().forEach(order -> {
//                // ????????????????????????
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
