package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface DishFlavorDAO extends BaseMapper<DishFlavor> {

    @Select("select id, name, value from dish_flavor where dish_id = #{id};")
    List<DishFlavor> listDishFlavor(Long id);

    @Select("insert into dish_flavor (dish_id, name, value, create_time, update_time, create_user, update_user) " +
            "VALUES (#{dishId}, #{name}, #{value}, #{createTime}, #{createTime}, #{createUser}, #{createUser})")
    int insertDishFlavor(DishFlavor flavors);

    void deleteNoIn(@Param("ids") List<Long> ids, @Param("dishId") Long dishId);

    void updateDishFlavor(DishFlavor dishFlavor);

    @Insert("insert into dish_flavor (dish_id, name, value, create_time, update_time, create_user, update_user) " +
            "value (#{dishId}, #{name}, #{value}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}) " +
            "on duplicate key update value = #{value}, update_time = #{updateTime}, update_user = #{updateUser}")
    void insertOrUpdate(DishFlavor flavor);

    @Delete("delete from dish_flavor where dish_id = #{id} ;")
    void deleteByDishId(Long id);

}
