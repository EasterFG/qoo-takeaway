package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.domain.ComboDish;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.query.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * @author EasterFG on 2022/9/28
 */
public interface ComboService {

    Combo getCombo(Long id);

    PageData<Combo> listCombo(PageQuery query, String name, Long categoryId, Integer status);

    List<Combo> listCombo(Long categoryId);

    /**
     * 保存套餐数据
     *
     * @param combo 套餐
     * @return 是否成功
     */
    boolean saveCombo(Combo combo);

    /**
     * 更新菜品, 不论是否要更新, 必传ud
     *
     * @param combo 套餐数据
     * @return 是否成功
     */
    boolean updateCombo(Combo combo);

    List<ComboDish> getDishCombo(Long comboId);

    void deleteByIds(Collection<Long> ids);

    boolean updateComboStatus(Long id, Integer status);
}
