package com.usermicroservice.repository.Impl;

import com.usermicroservice.domain.User;
import com.usermicroservice.repository.UserCustomRepository;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by dani on 2017-02-22.
 */
public class UserRepositoryImpl implements UserCustomRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Autowired
    private SessionFactory factory;

    @Override
    public List<User> getUsers(Long start, Long end) {
        Session session = factory.getCurrentSession();
        logger.info("before running query! session: "+session.isOpen());
        Query q = session.createQuery("SELECT us FROM com.usermicroservice.domain.User us");
        q.setFirstResult(start.intValue());
        q.setMaxResults(end.intValue());
        logger.info("After setting criteria");
        List<User> userList = q.list();
        logger.info("returning result");
        return userList;
    }

    @Override
    public User findByUsername(String username) {
        Session session = factory.getCurrentSession();
        Query q = session.createQuery("SELECT us FROM com.usermicroservice.domain.User us WHERE us.username=:username");
        q.setParameter("username",username);
        logger.info("After setting criteria");
        User userToReturn = (User)q.list().get(0);
        logger.info("returning result");
        return userToReturn;
    }

    @Override
    public List<User> findUsersByRoleId(int i) {
       //want to return all the User with the roleId;
        return null;
    }

}
