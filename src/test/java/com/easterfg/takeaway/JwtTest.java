package com.easterfg.takeaway;

import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.dao.StatisticsDAO;
import com.easterfg.takeaway.dao.UserDAO;
import com.easterfg.takeaway.domain.OrderStatusStatistics;
import com.easterfg.takeaway.domain.Statistics;
import com.easterfg.takeaway.utils.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author EasterFG on 2022/9/25
 */
@SpringBootTest
public class JwtTest {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserDAO userDAO;
    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private StatisticsDAO statisticsDAO;

    @Test
    void getJwt() {
        String token = jwtUtil.generateToken(1L, "admin", "管理员", "ADMIN", "USER");
        System.out.println(token);
        Claims claims = jwtUtil.getClaimsByToken(token);
        System.out.println(claims);
        System.out.println("claims.get(\"role\", List.class) = " + claims.get("role", List.class));
//        System.out.println(JwtUtils.generateJwt(1L, "admin", "管理员", "123"));
    }

    @Test
    void help() {
        LocalDateTime time = LocalDateTime.of(2022, 11, 13, 0, 0);
        for (int i = 13; i <= 19; i++) {
            OrderStatusStatistics os = orderDAO.statisticsByDate(time);
            BigDecimal decimal = orderDAO.statisticsAmount(time);
            int userAmount = userDAO.countUserByDate(time);
            Statistics statistics = new Statistics();
            statistics.setTurnover(decimal == null ? BigDecimal.ZERO : decimal);
            statistics.setTotalOrder(os.getTotal());
            statistics.setCancelOrder(os.getCancel());
            statistics.setCompleteOrder(os.getComplete());
            statistics.setCreateTime(time.toLocalDate());
            statistics.setUserCount(userAmount);
            statisticsDAO.insertStatistics(statistics);
            time = time.plusDays(1);
        }

    }

}
