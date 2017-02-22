package com.usermicroservice.service;

import com.usermicroservice.domain.Role;
import com.usermicroservice.domain.User;
import com.usermicroservice.domain.UserRole;

import java.util.List;

/**
 * Created by dani on 2017-02-22.
 */
public interface UserService {
    User findByUserNameAndPassword(String username, String password);
    User findUserById(Long id);
    User findByUsername(String username);
    User addUser(User newUser);
    Role getRole(String role);
    UserRole addUserRole(UserRole newUserRole);
    User updateUser(User user);

    //ADMIN
    List<User> findAllUsers(Long startPosition, Long endPosition);
}
