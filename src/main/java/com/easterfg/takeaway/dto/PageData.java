package com.easterfg.takeaway.dto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author EasterFG on 2022/11/9
 * <p>
 * 分页数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageData<T> {

    private long total;

    private List<T> records;

    public PageData(PageInfo<T> info) {
        this.total = info.getTotal();
        this.records = info.getList();
    }

}
