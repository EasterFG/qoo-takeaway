package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.AddressBook;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.service.AddressBookService;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author EasterFG on 2022/10/23
 */
@Authorize(Role.USER)
@RestController
@RequestMapping("/address")
@Api(tags = "地址接口")
public class AddressBookController {

    @Resource
    private AddressBookService addressBookService;

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result getDefaultAddress() {
        AddressBook defaultAddress = addressBookService.getDefaultAddress();
        if (defaultAddress == null) {
            return Result.failed("A0405", "未设置默认地址");
        }
        return Result.success("success", defaultAddress);
    }

    @GetMapping("/list")
    public Result listAddress() {
//        Long uid = UserContext.getUserId();
//        LambdaQueryWrapper<AddressBook> wrapper = Wrappers.lambdaQuery(AddressBook.class);
//        wrapper.select(AddressBook::getId, AddressBook::getConsignee, AddressBook::getDetail, AddressBook::getPhone,
//                AddressBook::getDefaults, AddressBook::getLabel, AddressBook::getGender, AddressBook::getCity);
//        wrapper.eq(AddressBook::getUserId, uid);
        return Result.success(addressBookService.listAddress());
    }

    @GetMapping("{id}")
    public Result getAddress(@PathVariable Long id) {
        AddressBook address = addressBookService.getAddress(id);
        if (address == null) {
            return Result.failed("服务器繁忙,请稍后再试");
        }
        return Result.success(address);
    }

    @PutMapping("{id}")
    public Result editAddress(@PathVariable Long id, @RequestBody @Validated(UpdateOperate.class) AddressBook addressBook) {
//        Long uid = UserContext.getUserId();
//        LambdaQueryWrapper<AddressBook> wrapper = Wrappers.lambdaQuery(AddressBook.class);
//        wrapper.eq(AddressBook::getUserId, uid).eq(AddressBook::getId, id);
//        addressBookService.updateAddress(addressBook);
//        boolean update = addressBookService.update(addressBook, wrapper);
        Long uid = UserContext.getUserId();
        addressBook.setId(id);
        addressBook.setUserId(uid);
        addressBook.setUpdateTime(LocalDateTime.now());
        if (addressBookService.updateAddress(addressBook) > 0) {
            return Result.success();
        }
        return Result.failed("A0403", "访问被拒绝");
    }

    @PostMapping
    public Result addAddress(@RequestBody @Validated(AddOperate.class) AddressBook addressBook) {
        if (addressBookService.saveAddress(addressBook) > 0) {
            return Result.success();
        }
        return Result.failed("A1071", "保存失败");
    }

    /**
     * 设置默认地址
     *
     * @param id 地址的id
     */
    @PutMapping("/default/{id}")
    public Result setDefaultAddress(@PathVariable Long id) {
        if (addressBookService.changeDefaultAddress(id) > 0) {
            return Result.success();
        }
        return Result.failed("A1072", "修改默认地址失败");
    }

    @DeleteMapping("{id}")
    public Result deleteAddress(@PathVariable Long id) {
        if (addressBookService.deleteAddress(id) > 0) {
            return Result.success();
        }
        return Result.failed("A1072", "删除地址失败");
    }

}
