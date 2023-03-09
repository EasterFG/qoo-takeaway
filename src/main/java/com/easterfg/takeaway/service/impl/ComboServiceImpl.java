package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.ComboDAO;
import com.easterfg.takeaway.dao.ComboDishDAO;
import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.domain.ComboDish;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.ComboService;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author EasterFG on 2022/9/28
 */
@Service
@Slf4j
public class ComboServiceImpl implements ComboService {

    @Resource
    private ComboDishDAO comboDishDAO;

    @Resource
    private ComboDAO comboDAO;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

//    private CategoryService categoryService;

    @Override
    public PageData<Combo> listCombo(PageQuery query, String name, Long categoryId, Integer status) {
        log.info("query param name = {}, cid = {}, status = {}", name, categoryId, status);
        Combo combo = new Combo();
        combo.setName(name);
        combo.setCategoryId(categoryId);
        combo.setStatus(status);
        PageMethod.startPage(query.getPage(), query.getPageSize());
        List<Combo> list = comboDAO.listCombo(combo);
        PageInfo<Combo> info = new PageInfo<>(list);
        return new PageData<>(info);
    }

    @Cacheable(value = "combo", key = "#categoryId")
    @Override
    public List<Combo> listCombo(Long categoryId) {
        return comboDAO.listComboByCid(categoryId);
    }

    @Override
    public Combo getCombo(Long id) {
        Combo combo = comboDAO.getCombo(id);
        // 获取套餐
        if (combo == null) {
            throw new BusinessException("套餐不存在");
//            return Result.failed(ErrorCode.COMBO_NOT_EXISTS);
        }
        combo.setComboDishes(comboDishDAO.listByComboId(id));
        return combo;
        // 查询对应分类名称
//        Category category = categoryMapper.selectById(combo.getCategoryId());
        // 获取套餐数据
//        ComboDTO comboDTO = new ComboDTO();
        // 复制有用的属性
//        BeanUtils.copyProperties(combo, comboDTO);
        // 设置分类名称
//        comboDTO.setCategoryName(category.getName());
//        LambdaQueryWrapper<ComboDish> wrapper = Wrappers.lambdaQuery(ComboDish.class);
//        wrapper.select(ComboDish::getId,
//                ComboDish::getCopies,
//                ComboDish::getDishId,
//                ComboDish::getName,
//                ComboDish::getPrice);
//        wrapper.eq(ComboDish::getComboId, id);
//        List<ComboDish> list = comboDishService.list(wrapper);
//        if (Objects.nonNull(list) && !list.isEmpty()) {
//            List<ComboDishDTO> collect = list.stream().map(item -> {
//                ComboDishDTO dto = new ComboDishDTO();
//                BeanUtils.copyProperties(item, dto);
//                return dto;
//            }).collect(Collectors.toList());
//            comboDTO.setComboDishes(collect);
//        }
//        return Result.success(comboDTO);
    }

    @Override
    @Transactional
    public boolean updateCombo(Combo combo) {
        Long uid = UserContext.getUserId();
        combo.setUpdateUser(uid);
        // 修改套餐
        comboDAO.updateCombo(combo);
//        this.updateById(combo);
        // 更新套餐
        // 删除数据, 删除不在id中的数据(set去重), 匹配comboId,
        List<ComboDish> comboDishes = combo.getComboDishes();
        Set<Long> ids = comboDishes.stream().map(ComboDish::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        comboDishDAO.deleteNoId(ids, combo.getId());
        comboDishes.forEach(comboDish -> {
            comboDish.setUpdateUser(uid);
            if (comboDish.getId() == null) {
                // 插入 or 更新
                comboDish.setComboId(combo.getId());
                comboDish.setCreateUser(uid);
                comboDishDAO.saveOrUpdate(comboDish);
            } else {
                comboDishDAO.updateComboDish(comboDish);
//                comboDishDAO.updateById(comboDish);
            }
        });
//        LambdaQueryWrapper<ComboDish> wrapper = Wrappers.lambdaQuery(ComboDish.class)
//                .eq(ComboDish::getComboId, combo.getId())
//                .notIn(ComboDish::getId, ids);
//        comboDishDAO.delete(wrapper);
        // 修改或查找数据

        return true;
//        List<ComboDishDTO> comboDishes = comboDTO.getComboDishes();
//        // 修改菜品
//        if (Objects.nonNull(comboDishes) && !comboDishes.isEmpty()) {
//            if (comboDishes.size() == 1 && comboDishes.get(0).getDelete()) {
//                // 长度为1, 且删除标记为true
//                return Result.failed("A0522", "套餐中至少要有一种菜品");
//            }
//            // 判断对应菜品数据是否移除, 物理删除
//            comboDishes.forEach(comboDishDTO -> {
//                ComboDish comboDish = new ComboDish();
//                BeanUtils.copyProperties(comboDishDTO, comboDish);
//                // 手动设置comboId
//                comboDish.setComboId(comboDTO.getId());
//                if (Objects.isNull(comboDishDTO.getDelete()) || !comboDishDTO.getDelete()) {
//                    // insert or update
//                    LambdaUpdateWrapper<ComboDish> wrapper = Wrappers.lambdaUpdate(ComboDish.class);
//                    wrapper.set(ComboDish::getCopies, comboDish.getCopies())
//                            .eq(ComboDish::getComboId, comboDish.getCopies())
//                            .eq(ComboDish::getDishId, comboDish.getDishId());
//                    // 尝试新增 or 更新  根据主键 和 联合唯一约束校验
//                    comboDishService.saveOrUpdate(comboDish, wrapper);
//
//                } else {
//                    // 删除数据
//                    comboDishService.removeById(comboDish.getId());
//                }
//            });

//            comboDishes.stream().map(dish -> {
//                ComboDish comboDish = new ComboDish();
//                BeanUtils.copyProperties(dish, comboDish);
//                return comboDish;
//            }).forEach(comboDish -> {
//                //
//                //
//                if (Objects.isNull(comboDish.getId())) {
//                    // 新增, 菜品从后端查询, 保证数据一致性
//                    Dish dish = dishMapper.selectById(comboDish.getDishId());
//                    comboDish.setName(dish.getName());
//                    comboDish.setPrice(dish.getPrice());
//                    comboDishService.save(comboDish);
//                } else {
//                    //
//                    // 如果菜品名称为null -> 删除
//                    if (StringUtils.hasLength(comboDish.getName())) {
//                        //删除
//                        comboDishService.removeById(comboDish.getId());
//                    } else {
//                        // 更新吗, 保证数据一致性
//                        LambdaUpdateWrapper<ComboDish> wrapper = Wrappers.lambdaUpdate(ComboDish.class);
//                        wrapper.set(ComboDish::getCopies, comboDish.getCopies())
//                                .eq(ComboDish::getId, comboDish.getDishId())
//                                .eq(ComboDish::getName, comboDish.getName());
//                        comboDishService.update(wrapper);
//                    }
//                }
//            });
//        }
//        return Result.success("修改成功");
    }

    @Override
    @Transactional
    public boolean saveCombo(Combo combo) {
        // 保存数据 批量 保存
        int row = comboDAO.insertCombo(combo);
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            ComboDishDAO mapper = sqlSession.getMapper(ComboDishDAO.class);
            long count = combo.getComboDishes().stream().map(mapper::insertComboDish).count();
            return row > 0 && count == combo.getComboDishes().size();
        }
    }

    @Override
    public List<ComboDish> getDishCombo(Long comboId) {
        return comboDAO.getComboDish(comboId);
    }

    @Override
    @Transactional
    public void deleteByIds(Collection<Long> ids) {
        if (ids.size() > 40) {
            throw new BusinessException("单次批量删除最大数量为40");
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            ComboDAO mapper = sqlSession.getMapper(ComboDAO.class);
            long count = ids.stream().map(mapper::deleteById).count();
            if (count < ids.size()) {
                throw new BusinessException("起售中的套餐不允许删除");
            }
        }
    }

    @Override
    public boolean updateComboStatus(Long id, Integer status) {
        Long uid = UserContext.getUserId();
        Combo combo = new Combo();
        combo.setId(id);
        combo.setStatus(status);
        combo.setUpdateUser(uid);
        return comboDAO.updateCombo(combo) > 0;
    }
}
