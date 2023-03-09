package com.easterfg.takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easterfg.takeaway.dao.DishFlavorDAO;
import com.easterfg.takeaway.domain.DishFlavor;
import com.easterfg.takeaway.service.DishFlavorService;
import com.easterfg.takeaway.utils.security.UserContext;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author EasterFG on 2022/9/27
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorDAO, DishFlavor> implements DishFlavorService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

//    @Resource
//    private DishFlavorDAO dishFlavorDAO;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDishFlavor(List<DishFlavor> list, Long dishId) {
        UserContext.User user = UserContext.getUser();
        Long uid = user.getId();
        List<Long> ids = list.stream().map(DishFlavor::getId)
                // 忽略空值
                .filter(Objects::nonNull).collect(Collectors.toList());
        // 不关sql session
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        DishFlavorDAO dishFlavorDAO = sqlSession.getMapper(DishFlavorDAO.class);
        // 先删除数据   防止插入的数据被删除
        dishFlavorDAO.deleteNoIn(ids, dishId);
        if (!list.isEmpty()) {
            // 让后新增和修改
            LocalDateTime now = LocalDateTime.now();
            for (DishFlavor flavor : list) {
                flavor.setDishId(dishId);
                flavor.setUpdateTime(now);
                flavor.setUpdateUser(uid);
                if (flavor.getId() == null) {
                    // 保存 or 新增
                    flavor.setCreateTime(now);
                    flavor.setCreateUser(uid);
                    dishFlavorDAO.insertOrUpdate(flavor);
                } else {
                    dishFlavorDAO.updateDishFlavor(flavor);
                }
            }
        }
    }
}
/*
 list.forEach(flavor -> {
 //                // 先尝试更新
 //                if (Objects.isNull(flavor.getId())) {
 //                    // 不存在ID, 先尝试更新
 ////                    LambdaUpdateWrapper<DishFlavor> wrapper = Wrappers.lambdaUpdate(DishFlavor.class);
 ////                    wrapper.eq(DishFlavor::getDishId,dishId).eq(DishFlavor::getName, flavor.getName())
 ////                            .set(DishFlavor::getValue, flavor.getValue());
 ////                    boolean ok = update(wrapper);
 ////                    if (!ok) {
 //                    // 更新失败, 尝试添加
 //                    flavor.setDishId(dishId);
 //                    save(flavor);
 ////                    }
 //                } else {
 //                    // 存在ID, 只更新value
 //                    LambdaUpdateWrapper<DishFlavor> updateWrapper = Wrappers.lambdaUpdate(DishFlavor.class)
 //                            .eq(DishFlavor::getId, flavor.getId())
 //                            .set(DishFlavor::getValue, flavor.getValue());
 //                    update(updateWrapper);
 //                }
 //            });
 }
 //            list.stream().map(f -> {
 //                DishFlavor flavor = new DishFlavor();
 //                // 设置ID
 //                flavor.setId(f.getId());
 //                flavor.setDishId(dishId);
 //                flavor.setName(f.getName());
 //                flavor.setValue(f.getValue());
 //                return flavor;
 //            }).forEach(dishFlavor -> {
 //                // 通过ID执行不同操作
 //                if (Objects.isNull(dishFlavor.getId())) {
 //                    // 新增
 //                    this.save(dishFlavor);
 //                } else {
 //                    if (Objects.isNull(dishFlavor.getValue())) {
 //                        // 删除
 //                        this.removeById(dishFlavor.getDishId());
 //                    } else {
 //                        // 更新, name不进行更新
 //                        LambdaUpdateWrapper<DishFlavor> wrapper = Wrappers.lambdaUpdate(DishFlavor.class);
 //                        wrapper.eq(DishFlavor::getId, dishFlavor.getId())
 //                                .set(DishFlavor::getValue, dishFlavor.getValue());
 //                        this.update(wrapper);
 //                    }
 //                }
 //            });
 }
 */
