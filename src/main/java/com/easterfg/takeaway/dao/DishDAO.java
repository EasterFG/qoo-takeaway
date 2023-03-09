package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Dish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface DishDAO {

    /**
     * 通过分类id删除数据, 逻辑删除
     */
    @Update("update dish set is_deleted = 1 where category_id = #{id}")
    void deleteByCategoryId(Long id);


    /**
     * 分页查询菜品, 不查询口味信息
     *
     * @param dish 查询条件
     * @return 查询结果
     */
    List<Dish> listDish(Dish dish);

    @Insert("insert into dish (name, category_id, price, code, image, description, create_time, update_time, create_user, update_user)" +
            " values (#{name}, #{categoryId},#{price}, null, #{image}, #{description}, now(), now(), #{createUser}, #{createUser});")
    int insertDish(Dish dish);


    /**
     * 通过分类id查询数据
     */
    List<Dish> listDishByCid(Long cid);

    /**
     * 删除菜品, 禁售菜品
     *
     * @param id 菜品id
     */
    int deleteByIds(Collection<Long> id);

    @Select("select count(*) from dish where is_deleted = 0 and category_id = #{cid};")
    int countByCid(Long cid);

    @Select("select * from dish where id = #{id} and is_deleted = 0;")
    Dish findById(Long id);

    int updateDish(Dish dish);

    @Delete("DELETE from dish where id = #{id} and status = 0")
    int deleteById(Long id);

}
