package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.dao.StatisticsDAO;
import com.easterfg.takeaway.dao.UserDAO;
import com.easterfg.takeaway.domain.Statistics;
import com.easterfg.takeaway.domain.StatusStatistics;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.service.StatisticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
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
        LocalDate time = LocalDate.now().minusDays(1);
        List<StatusStatistics> lists = orderDAO.statisticsByDate(time);
        BigDecimal decimal = orderDAO.statisticsAmount(time);
        int userAmount = userDAO.countUserByDate(time);
        int total = 0;
        Statistics statistics = new Statistics();
        for (StatusStatistics status : lists) {
            total += status.getCount();
            switch (status.getStatus()) {
                case FINISHED:
                    statistics.setCompleteOrder(status.getCount());
                    break;
                case CANCELLED:
                    statistics.setCancelOrder(status.getCount());
                    break;
            }
        }
        statistics.setTotalOrder(total);
        statistics.setTurnover(decimal);
        statistics.setCreateTime(time);
        statistics.setUserCount(userAmount);
        // 插入报错
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

    @Override
    public List<Statistics> statistics(LocalDate start, LocalDate end) {
        if (ChronoUnit.DAYS.between(start, end) > 30) {
            throw new BusinessException("时间查超过30天");
        }
        return statisticsDAO.listStatistics(start, end);
    }

}
