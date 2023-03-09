package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.AddressBook;

import java.util.List;

/**
 * @author EasterFG on 2022/10/23
 */
public interface AddressBookService {

    AddressBook getAddress(Long id);

    int changeDefaultAddress(Long id);

    AddressBook getDefaultAddress();

    List<AddressBook> listAddress();

    int updateAddress(AddressBook addressBook);

    int saveAddress(AddressBook addressBook);

    int deleteAddress(Long id);
}
