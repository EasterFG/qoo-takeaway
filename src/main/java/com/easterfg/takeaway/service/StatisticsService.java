package com.easterfg.takeaway.service;


import com.easterfg.takeaway.domain.Statistics;

import java.time.LocalDate;
import java.util.List;

/**
 * @author EasterFG on 2022/11/19
 */
public interface StatisticsService {

    /**
     * 自动统计
     */
    void autoStatistics();

    List<Statistics> getStatistics(int flag);

    List<Statistics> statistics(LocalDate start, LocalDate end);
}
