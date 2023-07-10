package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Statistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * @author EasterFG on 2022/11/19
 */
public interface StatisticsDAO {

    @Insert("insert into `statistics` (turnover, total_order, complete_order, cancel_order, user_count, create_time) values" +
            "(#{turnover}, #{totalOrder}, #{completeOrder}, #{cancelOrder}, #{userCount}, #{createTime})")
    void insertStatistics(Statistics statistics);

    @Select("<script>" +
            "select turnover, total_order, complete_order, cancel_order, user_count, create_time " +
            "from statistics" +
            "<where>" +
            "    <if test='start != null and end != null'>" +
            "        create_time between #{start} and #{end}" +
            "    </if>" +
            "    <if test='start == null and end != null'>" +
            "        and create_time = #{end}" +
            "    </if>" +
            "</where>" +
            "</script>")
    List<Statistics> listStatistics(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("select max(create_time) from statistics limit 1")
    LocalDate lastCreateTime();
}
