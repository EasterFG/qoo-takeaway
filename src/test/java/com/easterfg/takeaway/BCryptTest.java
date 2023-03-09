package com.easterfg.takeaway;

import com.easterfg.takeaway.utils.security.BCrypt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author EasterFG on 2022/9/25
 */
public class BCryptTest {

    @Test
    public void test() {
        String gensalt = BCrypt.gensalt(10);
        System.out.println(gensalt);
        String password = BCrypt.hashpw("123456", gensalt);
        assertTrue(BCrypt.checkpw("123456", password));
    }

}
