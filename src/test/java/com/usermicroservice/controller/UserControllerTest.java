package com.usermicroservice.controller;

import com.usermicroservice.domain.Role;
import com.usermicroservice.domain.User;
import com.usermicroservice.domain.UserRole;
import com.usermicroservice.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by dani on 2017-02-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void getUser() throws Exception {
        Role role = new Role();
        role.setRole("AUCTIONEER");
        role.setRoleID(1);

        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUserRoleId(1L);

        Set<UserRole> userRoleSet = new HashSet<>();
        userRoleSet.add(userRole);


        User user = new User();
        user.setId(1L);
        user.setUsername("anders");
        user.setPassword("123");
        user.setEmail("anders@kth.se");
        user.setUserRole(userRoleSet);

        when(userService.findUserById(1L)).thenReturn(user);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string("{\"id\":1,\"username\":\"anders\",\"firstname\":null,\"lastname\":null,\"email\":\"anders@kth.se\",\"userRoles\":[\"AUCTIONEER\"]}"));

    }
}