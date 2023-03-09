package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.domain.ComboDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author EasterFG on 2022/9/28
 */
public interface ComboDAO {

    List<Combo> listCombo(Combo combo);

    List<ComboDish> getComboDish(Long comboId);

    @Select("select count(*) from combo where is_deleted = 1 and category_id = #{cid}")
    int countByCid(Long cid);

    @Select("select id, name, price, description, image from combo where category_id = #{categoryId} and is_deleted = 0 and status = 1")
    List<Combo> listComboByCid(Long categoryId);

    @Select("select id, category_id, name, price, status, description, image, update_time from combo where id = #{id} and is_deleted = 0")
    Combo getCombo(Long id);

    int updateCombo(Combo combo);

    @Insert("insert into combo (category_id, name, price, description, image, create_time, update_time, create_user, update_user) " +
            "VALUES (#{categoryId}, #{name}, #{price}, #{description}, #{image}, now(), now(), #{createUser}, #{createUser})")
    int insertCombo(Combo combo);

    @Update("update combo set is_deleted = 1 where is_deleted = 0 and status = 0 and id = #{id}")
    int deleteById(Long id);
}
