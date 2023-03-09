package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.dao.StatisticsDAO;
import com.easterfg.takeaway.dao.UserDAO;
import com.easterfg.takeaway.domain.OrderStatusStatistics;
import com.easterfg.takeaway.domain.Statistics;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.service.StatisticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;
import java.util.List;

/**
 * @author EasterFG on 2022/11/19
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Resource
    private StatisticsDAO statisticsDAO;
    @Resource
    private OrderDAO orderDAO;

    @Resource
    private UserDAO userDAO;

    @Scheduled(cron = "0 0 0 * * ?")
    @Override
    public void autoStatistics() {
        // 每天凌晨统计上一日数据
        // 获取当前时间的前一, 确保时间一定是当天的00:00
        LocalDateTime time = LocalDateTime.from(LocalDate.now().minusDays(1));
        OrderStatusStatistics os = orderDAO.statisticsByDate(time);
        BigDecimal decimal = orderDAO.statisticsAmount(time);
        int userAmount = userDAO.countUserByDate(time);
        Statistics statistics = new Statistics();
        statistics.setTurnover(decimal);
        statistics.setTotalOrder(os.getTotal());
        statistics.setCancelOrder(os.getCancel());
        statistics.setCompleteOrder(os.getComplete());
        statistics.setCreateTime(time.toLocalDate());
        statistics.setUserCount(userAmount);
        statisticsDAO.insertStatistics(statistics);
    }

    @Override
    public List<Statistics> getStatistics(int flag) {
        LocalDate start = null;
        LocalDate end = LocalDate.now().minusDays(1);
        // 数据统计
        switch (flag) {
            case 0:
                // 啥都不用干
                break;
            case 1:
                start = end.minusDays(6);
                break;
            case 2:
                start = end.minusDays(29);
                break;
            case 3:
                // 本周
                start = end.with(ChronoField.DAY_OF_WEEK, 1);
                end = end.with(ChronoField.DAY_OF_WEEK, 7);
                break;
            case 4:
                ValueRange range = end.range(ChronoField.DAY_OF_MONTH);
                start = end.with(ChronoField.DAY_OF_MONTH, range.getMinimum());
                end = end.with(ChronoField.DAY_OF_MONTH, range.getMaximum());
                break;
            default:
                throw new BusinessException("用户参数错误");
        }
        return statisticsDAO.listStatistics(start, end);
    }

}
