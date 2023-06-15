package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.ComboDishDAO;
import com.easterfg.takeaway.dao.DishDAO;
import com.easterfg.takeaway.dao.DishFlavorDAO;
import com.easterfg.takeaway.domain.Dish;
import com.easterfg.takeaway.domain.DishFlavor;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.DishFlavorService;
import com.easterfg.takeaway.service.DishService;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
@Service
public class DishServiceImpl implements DishService {

    @Resource
    private DishFlavorService dishFlavorService;
    @Resource
    private ComboDishDAO comboDishDAO;
    @Resource
    private DishDAO dishDAO;
    @Resource
    private DishFlavorDAO dishFlavorDAO;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageData<Dish> listDish(PageQuery query, String name, Long categoryId, Integer status) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setCategoryId(categoryId);
        dish.setStatus(status);
        PageMethod.startPage(query.getPage(), query.getPageSize());
        List<Dish> list = dishDAO.listDish(dish);
        PageInfo<Dish> info = new PageInfo<>(list);
        return new PageData<>(info);
    }

    @Override
//    @Cacheable(value = "dishCache", key = "#id", unless = "#result == null")
    public Dish getDish(Long id) {
        // 查询Dish
        Dish dish = dishDAO.findById(id);
        if (dish == null) {
            return null;
        }
        // 查询口味
        List<DishFlavor> flavors = dishFlavorDAO.listDishFlavor(dish.getId());
        dish.setFlavors(flavors);
        return dish;
    }

    @Override
    public List<Dish> listDish(Long categoryId) {
        // 通过分类查询菜品
        return dishDAO.listDishByCid(categoryId);
    }

    @Override
    @Transactional
    public void saveDish(Dish dish) {
        Long uid = UserContext.getUserId();
        dish.setCreateUser(uid);
        // 验证分类 是否存在由 jsr303 校验
        int row = dishDAO.insertDish(dish);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            DishFlavorDAO mapper = sqlSession.getMapper(DishFlavorDAO.class);
            LocalDateTime now = LocalDateTime.now();
            for (DishFlavor item : dish.getFlavors()) {
                item.setDishId(dish.getId());
                item.setCreateTime(now);
                item.setCreateUser(uid);
                row += mapper.insertDishFlavor(item);
            }
            if (row < dish.getFlavors().size() + 1) {
                throw new BusinessException("新增菜品失败");
            }
        }
    }

    /**
     * 不论数据是否更新, 都需要传id进来, 不传递的id认为被删除了
     */
    @Override
    @Transactional
    public boolean updateDish(Dish dish) {
        UserContext.User user = UserContext.getUser();
        Long uid = user.getId();
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(uid);
        List<DishFlavor> flavors = dish.getFlavors();
        dishDAO.updateDish(dish);
        // 更新
        dishFlavorService.updateDishFlavor(flavors, dish.getId());
        // 更新冗余数据 -> 套餐菜品字段 (如果name 和 price不为空, 才更新)
        comboDishDAO.updateDishField(dish);
        return true;
    }

    @Override
    public boolean updateDishStatus(Long id, Integer status) {
        Long uid = UserContext.getUserId();
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(uid);
        return dishDAO.updateDish(dish) > 0;
    }

    @Transactional
    @Override
    public int removeDish(Collection<Long> ids) {
        if (ids.size() > 40) {
            throw new BusinessException("40002", "单次最大删除40条数据");
        }
        int count = 0;
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            DishDAO mapper = sqlSession.getMapper(DishDAO.class);
            DishFlavorDAO flavorMapper = sqlSession.getMapper(DishFlavorDAO.class);
            for (Long id : ids) {
                count += mapper.deleteById(id);
                flavorMapper.deleteByDishId(id);
            }
        }
        return count;
//        int rows = getBaseMapper().deleteByIds(ids);
//        // 删除对应口味数据
//       dishFlavorService.removeByIds()
//        dishFlavorService.removeByIds(ids);
//        return rows;
    }
}
