package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.AddressBook;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author EasterFG on 2022/10/23
 */
public interface AddressBookDAO {

    @Update("UPDATE address_book SET is_defaults = IF(id = #{id}, 1, 0) WHERE user_id = #{userId}")
    int updateDefaultAddress(@Param("id") Long id, @Param("userId") Long userId);

    @Select("select id, consignee, gender, phone, code, city, detail, label from address_book where id = #{id} and user_id = #{uid} and is_deleted = 0")
    AddressBook selectById(@Param("id") Long id, @Param("uid") Long uid);

    @Select("select id, consignee, gender, phone, code, city, detail, label, is_defaults `defaults` from address_book where is_deleted = 0 and user_id = #{uid} and is_defaults = 1 limit 1;")
    AddressBook selectDefault(Long uid);

    @Select("select id, consignee, gender, phone, city, detail, label, is_defaults `defaults` from address_book where is_deleted = 0 and user_id = #{uid}")
    List<AddressBook> listAddress(Long uid);

    int updateAddress(AddressBook addressBook);

    @Insert("insert into address_book (user_id, consignee, gender, phone, code, city, detail, label, create_time, update_time) " +
            "values (#{userId}, #{consignee}, #{gender}, #{phone}, #{code}, #{city}, #{detail}, #{label}, now(),now());")
    int insertAddress(AddressBook addressBook);

    @Update("update address_book set is_deleted = 1 where is_deleted = 0 and user_id = #{uid} and id = #{id};")
    int deleteAddress(@Param("id") Long id, @Param("uid") Long uid);
}
