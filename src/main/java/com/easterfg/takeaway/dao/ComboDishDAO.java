package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.ComboDish;
import com.easterfg.takeaway.domain.Dish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * @author EasterFG on 2022/9/28
 */
public interface ComboDishDAO extends BaseMapper<ComboDish> {

    @Select("select id, combo_id, dish_id, name, price, copies, sort from combo_dish where combo_id = #{comboId};")
    List<ComboDish> listByComboId(Long comboId);

    @Insert("INSERT INTO combo_dish VALUES (null, #{comboId}, #{dishId}, #{name}, #{price}, #{copies}, 0, now(), now(), #{createUser}, #{createUser}) " +
            "ON DUPLICATE KEY UPDATE name = #{name},price = #{price}, copies = #{copies}, update_time = now(), update_user = #{updateUser}")
    void saveOrUpdate(ComboDish comboDish);

    @Update("update combo_dish set name = #{name}, price = #{price} where dish_id = #{id}")
    void updateDishField(Dish dish);

    void deleteNoId(@Param("ids") Collection<Long> ids, @Param("comboId") Long comboId);

    int updateComboDish(ComboDish comboDish);

    @Insert("insert into combo_dish (combo_id, dish_id, name, price, copies, create_time, update_time, create_user, update_user) " +
            "VALUES (#{comboId}, #{dishId}, #{name}, #{price}, #{copies}, now(), now(), #{createUser}, #{updateUser})")
    int insertComboDish(ComboDish comboDish);
}
