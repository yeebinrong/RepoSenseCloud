package com.hamburger.user.dao;

import com.hamburger.user.dao.database.entity.User;

public interface UserDao {
    void save(User user);
    void delete(String id);
    User findById(String id);
    User findByUserName(String userName);
}