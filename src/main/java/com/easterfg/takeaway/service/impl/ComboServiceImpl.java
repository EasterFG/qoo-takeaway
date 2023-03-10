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
        // ????????????
        if (combo == null) {
            throw new BusinessException("???????????????");
//            return Result.failed(ErrorCode.COMBO_NOT_EXISTS);
        }
        combo.setComboDishes(comboDishDAO.listByComboId(id));
        return combo;
        // ????????????????????????
//        Category category = categoryMapper.selectById(combo.getCategoryId());
        // ??????????????????
//        ComboDTO comboDTO = new ComboDTO();
        // ?????????????????????
//        BeanUtils.copyProperties(combo, comboDTO);
        // ??????????????????
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
        // ????????????
        comboDAO.updateCombo(combo);
//        this.updateById(combo);
        // ????????????
        // ????????????, ????????????id????????????(set??????), ??????comboId,
        List<ComboDish> comboDishes = combo.getComboDishes();
        Set<Long> ids = comboDishes.stream().map(ComboDish::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        comboDishDAO.deleteNoId(ids, combo.getId());
        comboDishes.forEach(comboDish -> {
            comboDish.setUpdateUser(uid);
            if (comboDish.getId() == null) {
                // ?????? or ??????
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
        // ?????????????????????

        return true;
//        List<ComboDishDTO> comboDishes = comboDTO.getComboDishes();
//        // ????????????
//        if (Objects.nonNull(comboDishes) && !comboDishes.isEmpty()) {
//            if (comboDishes.size() == 1 && comboDishes.get(0).getDelete()) {
//                // ?????????1, ??????????????????true
//                return Result.failed("A0522", "?????????????????????????????????");
//            }
//            // ????????????????????????????????????, ????????????
//            comboDishes.forEach(comboDishDTO -> {
//                ComboDish comboDish = new ComboDish();
//                BeanUtils.copyProperties(comboDishDTO, comboDish);
//                // ????????????comboId
//                comboDish.setComboId(comboDTO.getId());
//                if (Objects.isNull(comboDishDTO.getDelete()) || !comboDishDTO.getDelete()) {
//                    // insert or update
//                    LambdaUpdateWrapper<ComboDish> wrapper = Wrappers.lambdaUpdate(ComboDish.class);
//                    wrapper.set(ComboDish::getCopies, comboDish.getCopies())
//                            .eq(ComboDish::getComboId, comboDish.getCopies())
//                            .eq(ComboDish::getDishId, comboDish.getDishId());
//                    // ???????????? or ??????  ???????????? ??? ????????????????????????
//                    comboDishService.saveOrUpdate(comboDish, wrapper);
//
//                } else {
//                    // ????????????
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
//                    // ??????, ?????????????????????, ?????????????????????
//                    Dish dish = dishMapper.selectById(comboDish.getDishId());
//                    comboDish.setName(dish.getName());
//                    comboDish.setPrice(dish.getPrice());
//                    comboDishService.save(comboDish);
//                } else {
//                    //
//                    // ?????????????????????null -> ??????
//                    if (StringUtils.hasLength(comboDish.getName())) {
//                        //??????
//                        comboDishService.removeById(comboDish.getId());
//                    } else {
//                        // ?????????, ?????????????????????
//                        LambdaUpdateWrapper<ComboDish> wrapper = Wrappers.lambdaUpdate(ComboDish.class);
//                        wrapper.set(ComboDish::getCopies, comboDish.getCopies())
//                                .eq(ComboDish::getId, comboDish.getDishId())
//                                .eq(ComboDish::getName, comboDish.getName());
//                        comboDishService.update(wrapper);
//                    }
//                }
//            });
//        }
//        return Result.success("????????????");
    }

    @Override
    @Transactional
    public boolean saveCombo(Combo combo) {
        // ???????????? ?????? ??????
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
            throw new BusinessException("?????????????????????????????????40");
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            ComboDAO mapper = sqlSession.getMapper(ComboDAO.class);
            long count = ids.stream().map(mapper::deleteById).count();
            if (count < ids.size()) {
                throw new BusinessException("?????????????????????????????????");
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
