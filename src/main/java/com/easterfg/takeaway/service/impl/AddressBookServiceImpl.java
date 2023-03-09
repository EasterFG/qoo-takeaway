package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.AddressBookDAO;
import com.easterfg.takeaway.domain.AddressBook;
import com.easterfg.takeaway.service.AddressBookService;
import com.easterfg.takeaway.utils.security.UserContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/10/23
 */
@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Resource
    private AddressBookDAO addressBookDAO;

    @Override
    public AddressBook getAddress(Long id) {
        Long uid = UserContext.getUserId();
        return addressBookDAO.selectById(id, uid);
    }

    @Override
    public int changeDefaultAddress(Long id) {
        Long uid = UserContext.getUserId();
        return addressBookDAO.updateDefaultAddress(id, uid);
    }

    @Override
    public AddressBook getDefaultAddress() {
        Long uid = UserContext.getUserId();
        return addressBookDAO.selectDefault(uid);
    }

    @Override
    public List<AddressBook> listAddress() {
        Long uid = UserContext.getUserId();
        return addressBookDAO.listAddress(uid);
    }

    @Override
    public int updateAddress(AddressBook addressBook) {
        return addressBookDAO.updateAddress(addressBook);
    }

    @Override
    public int saveAddress(AddressBook addressBook) {
        Long uid = UserContext.getUserId();
        addressBook.setUserId(uid);
        return addressBookDAO.insertAddress(addressBook);
    }

    @Override
    public int deleteAddress(Long id) {
        Long uid = UserContext.getUserId();
        addressBookDAO.deleteAddress(id, uid);
        return 0;
    }
}
