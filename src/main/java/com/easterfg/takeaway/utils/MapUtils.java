package com.easterfg.takeaway.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author EasterFG on 2022/10/22
 */
public class MapUtils {

    private final Map<String, Object> map = new HashMap<>();

    public static MapUtils hashMap() {
        return new MapUtils();
    }

    public MapUtils put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return map;
    }

}
