package com.easterfg.takeaway;

import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.ComboService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author EasterFG on 2022/11/9
 */
@SpringBootTest
@Slf4j
class ComboTest {

    @Autowired
    private ComboService comboService;

    @Test
    void list() {
        PageQuery query = new PageQuery(0, 100);
        PageData<Combo> pageData = comboService.listCombo(query, null, null, null);
        assertNotNull(pageData);
    }

    @Test
    void getCombo() {
        Combo combo = comboService.getCombo(1415580119015145474L);
        log.info("combo = {}", combo);
        assertNotNull(combo);
    }
}
