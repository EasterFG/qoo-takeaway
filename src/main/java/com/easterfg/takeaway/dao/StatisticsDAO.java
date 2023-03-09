package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Statistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author EasterFG on 2022/11/19
 */
public interface StatisticsDAO {

    @Insert("insert into `statistics` (turnover, total_order, complete_order, cancel_order, user_count, create_time) values" +
            "(#{turnover}, #{totalOrder}, #{completeOrder}, #{cancelOrder}, #{userCount}, #{createTime})")
    void insertStatistics(Statistics statistics);

    List<Statistics> listStatistics(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
