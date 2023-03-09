package com.easterfg.takeaway;

import com.easterfg.takeaway.utils.SMSUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;

/**
 * @author EasterFG on 2022/11/19
 */
class TimeTest {

    @Test
    void dateTest() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.of(2022, 2, 19);
        ValueRange range = date.range(ChronoField.DAY_OF_MONTH);
        LocalDate min = date.with(ChronoField.DAY_OF_MONTH, range.getMinimum());
        LocalDate max = date.with(ChronoField.DAY_OF_MONTH, range.getMaximum());
        System.out.println("min = " + min);
        System.out.println("max = " + max);
//        Assertions.assertEquals("2022-11-14", formatter.format(with));
    }

    @Test
    void testRandom() {
        String code = SMSUtils.generateValidateCode();
        long start = System.currentTimeMillis();
        String code1 = SMSUtils.generateValidateCode();
        String code2 = SMSUtils.generateValidateCode();
        String code3 = SMSUtils.generateValidateCode();
        String code4 = SMSUtils.generateValidateCode();
        long l = System.currentTimeMillis();
        System.out.println(l - start);
        System.out.println(code1);
        System.out.println(code2);
        System.out.println(code3);
        System.out.println(code4);
    }
}
