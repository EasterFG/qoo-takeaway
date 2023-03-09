package com.easterfg.takeaway.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.easterfg.takeaway.utils.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author EasterFG on 2022/9/24
 */
@Component
@Slf4j
public class PlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // TODO 暂时设置未1
        log.info("user info is {}", UserContext.getUser());
        this.strictInsertFill(metaObject, "createUser", Long.class, 1L);
        this.strictInsertFill(metaObject, "updateUser", Long.class, 1L);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // TODO 暂时设置未1
        log.info("user info is {}", UserContext.getUser());
        this.strictUpdateFill(metaObject, "updateUser", Long.class, 1L);
    }
}
