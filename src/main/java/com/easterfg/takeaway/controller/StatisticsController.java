package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Statistics;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/11/18
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping("{flag}")
    public Result getStatistics(@PathVariable("flag") Integer flag) {
        List<Statistics> statistics = statisticsService.getStatistics(flag);
        return Result.success(statistics);
    }
}
