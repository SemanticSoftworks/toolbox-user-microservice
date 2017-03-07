package com.usermicroservice.repository;

import com.usermicroservice.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by dani on 2017-02-22.
 */
@Repository
public interface UserCustomRepository{
    List<User> getUsers(Long start, Long end);
    User findByUsername(String username);
    List<User> findUsersByRoleId(int i);
}