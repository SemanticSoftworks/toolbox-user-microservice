package com.usermicroservice.service.impl;

import com.usermicroservice.domain.Role;
import com.usermicroservice.domain.User;
import com.usermicroservice.domain.UserRole;
import com.usermicroservice.repository.RoleRepository;
import com.usermicroservice.repository.UserCustomRepository;
import com.usermicroservice.repository.UserRepository;
import com.usermicroservice.repository.UserRoleRepository;
import com.usermicroservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dani on 2017-02-22.
 */
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserCustomRepository userCustomRepository;

    @Override
    public User findByUserNameAndPassword(String username, String password) {
        User tmpUser = userRepository.findByUsername(username);

        if(tmpUser != null && tmpUser.isEnabled()){
            boolean correctPassword = BCrypt.checkpw(password, tmpUser.getPassword()); // om skickad hashad lösenord = databasens
            if(correctPassword){
                return tmpUser;
            }
        }
        return null;
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User addUser(User newUser) {
        User savedUser = null;

        User userCheck = userRepository.findByUsername(newUser.getUsername());
        if(userCheck == null)
            savedUser = userRepository.save(newUser);

        return savedUser;
    }

    @Override
    public Role getRole(String role) { return roleRepository.findByRole(role); }

    @Override
    public UserRole addUserRole(UserRole newUserRole) { return userRoleRepository.save(newUserRole); }

    @Override
    public User updateUser(User user) {

        if(user != null){
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public List<User> findAllUsers(Long startPosition, Long endPosition) { return userCustomRepository.getUsers(startPosition,endPosition); }

    @Override
    public Role addRole(Role newRole) {
        Role role = roleRepository.findByRole(newRole.getRole());
        if(role == null)
            return roleRepository.save(newRole);
        return null;
    }

    @Override
    public List<Role> getRoles() { return roleRepository.findAll(); }

    @Override
    public Role findRoleById(Integer id) { return roleRepository.findOne(id); }

    @Override
    public Role updateRole(Role updateRole) { return roleRepository.save(updateRole); }
}
