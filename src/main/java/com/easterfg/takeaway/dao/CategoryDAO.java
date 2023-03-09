package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface CategoryDAO {

    /**
     * 判断指定ID是存在
     */
    @Select("SELECT COUNT(*) FROM category WHERE id = #{id} LIMIT 1")
    int count(Long id);

    List<Category> listCategory(Category category);

    List<Category> listCategoryByType(Integer type);

    @Select("SELECT id, type, name, sort, status, update_time FROM category WHERE is_deleted = 0;")
    Category getCategoryById(Long id);

    @Insert("insert into category (type, name, sort, create_time, update_time, create_user, update_user) VALUES" +
            "(#{type}, #{name}, #{sort}, now(), now(), #{createUser}, #{createUser})")
    int insertCategory(Category category);

    int updateCategory(Category category);

    @Update("update category set is_deleted = 1, update_time = now() where id = #{id}")
    int deleteById(Long id);
}
