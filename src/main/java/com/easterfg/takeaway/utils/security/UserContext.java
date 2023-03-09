package com.easterfg.takeaway.utils.security;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author EasterFG on 2022/10/22
 * <p>
 * 用户上下文对象
 */
public class UserContext {

    private UserContext() {
    }

    /**
     * jwt解析对应user类
     */
    @Getter
    public static class User {
        private final Long id;
        private final String username;
        private final String name;
        private final List<Role> roles;

        public User(Long id, String username, String name, List<String> roles) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.roles = roles.stream().map(Role::valueOf).collect(Collectors.toList());
        }

        /**
         * 检查用户是否拥有指定权限
         *
         * @param role 等待判定的权限
         * @return 是否拥有此权限
         */
        public boolean hasRole(Role role) {
            if (roles.isEmpty()) {
                return false;
            }
            return roles.contains(role);
        }
    }

    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 获取用户对象
     *
     * @return 用户对象
     */
    public static User getUser() {
        return userThreadLocal.get();
    }

    /**
     * 获取用户id, 会销毁对应用户对象
     *
     * @return id
     */
    public static Long getUserId() {
        Long id = userThreadLocal.get().getId();
        destroy();
        return id;
    }

    /**
     * 设置User对象
     *
     * @param user User
     */
    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    /**
     * 销毁使用完成的对象,防止内存泄露
     */
    public static void destroy() {
        userThreadLocal.remove();
    }
}
