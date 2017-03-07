package com.usermicroservice.controller;

import com.usermicroservice.Hash;
import com.usermicroservice.domain.Role;
import com.usermicroservice.domain.User;
import com.usermicroservice.domain.UserRole;
import com.usermicroservice.model.AdminUserAdderDTO;
import com.usermicroservice.model.AdminUserDTO;
import com.usermicroservice.model.RoleDTO;
import com.usermicroservice.model.RoleListingDTO;
import com.usermicroservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Teddy on 2017-03-07.
 */
@RestController
@RequestMapping("/admin")
public class AdminController{


    @Autowired
    private UserService userService;

    @RequestMapping(value = "/admin", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<AdminUserDTO> Adminregister(@RequestBody AdminUserAdderDTO incomingUser) {
        AdminUserDTO userDTO = new AdminUserDTO();

        User userToAdd = userService.findByUsername(incomingUser.getUsername());
        if (userToAdd == null) {
            User newUser = new User();
            newUser.setUsername(incomingUser.getUsername());
            newUser.setPassword(Hash.BcryptEncrypt(incomingUser.getPassword()));
            newUser.setEmail(incomingUser.getEmail());
            newUser.setEnabled(incomingUser.isEnabled());
            newUser.setFirstName(incomingUser.getFirstname());
            newUser.setLastName(incomingUser.getLastname());

            User mockUser = userService.addUser(newUser);

            if (mockUser != null) {
                for (String role : incomingUser.getUserRoles()) {
                    Role realRole = userService.getRole(role);
                    if (realRole != null) {
                        UserRole newUserRole = new UserRole();
                        newUserRole.setUser(mockUser);
                        newUserRole.setRole(realRole);
                        userService.addUserRole(newUserRole);
                    }
                }
                userDTO.setId(mockUser.getId());
                userDTO.setUsername(mockUser.getUsername());
                userDTO.setEmail(mockUser.getEmail());
                userDTO.setFirstname(mockUser.getFirstName());
                userDTO.setLastname(mockUser.getLastName());
                userDTO.setUserRoles(extractUserRoles(mockUser.getUserRole()));
                userDTO.setEnabled(mockUser.isEnabled());
                userDTO.setPassword(mockUser.getPassword());

                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/accountActivation/{id}", method = RequestMethod.PUT)
    public ResponseEntity<AdminUserDTO> AdminaccountActivation(@PathVariable Long id , @RequestParam Boolean enable){
        AdminUserDTO userToReturn = new AdminUserDTO();

        User user = userService.findUserById(id);
        user.setEnabled(enable);
        user = userService.updateUser(user);

        if(user != null) {
            userToReturn.setId(user.getId());
            userToReturn.setUserRoles(extractUserRoles(user.getUserRole()));
            userToReturn.setUsername(user.getUsername());
            userToReturn.setEmail(user.getEmail());
            userToReturn.setFirstname(user.getFirstName());
            userToReturn.setLastname(user.getLastName());
            userToReturn.setEnabled(user.isEnabled());
            return new ResponseEntity<>(userToReturn, HttpStatus.OK);
        }
        return new ResponseEntity<>(userToReturn, HttpStatus.BAD_REQUEST);
    }

    private Set<UserRole> updateUserRoles(List<String> fakeRoles, User user){
        Set<UserRole> userRoles = new HashSet<>();

        // delete existing userRoles
        for(UserRole roleToDelete : user.getUserRole()){ userService.deleteUserRole(roleToDelete); }
        // add ones not there
        for(String userRole : fakeRoles){
            UserRole realUserRole = new UserRole();
            realUserRole.setUser(user);

            Role roleCheck =userService.getRole(userRole);
            if(roleCheck != null){
                realUserRole.setRole(roleCheck);
                realUserRole = userService.addUserRole(realUserRole);
                userRoles.add(realUserRole);
            }
        }
        return userRoles;
    }

    @RequestMapping(value="/update", method = RequestMethod.POST, consumes={"application/json"})
    public ResponseEntity<AdminUserDTO> AdminupdateUser(@RequestBody AdminUserDTO incomingUser){

        AdminUserDTO adminUserDTO= new AdminUserDTO();
        Set<UserRole> userRoles = new HashSet<>();

        User user = userService.findUserById(incomingUser.getId());
        user.setPassword(Hash.BcryptEncrypt(incomingUser.getPassword()));
        user.setEmail(incomingUser.getEmail());
        user.setFirstName(incomingUser.getFirstname());
        user.setLastName(incomingUser.getLastname());
        user.setEnabled(incomingUser.isEnabled());

        user.setUserRole(updateUserRoles(incomingUser.getUserRoles(),user));
        user = userService.updateUser(user);

        if (user != null) {
            adminUserDTO.setId(user.getId());
            adminUserDTO.setUsername(user.getUsername());
            adminUserDTO.setPassword(user.getPassword());
            adminUserDTO.setEmail(user.getEmail());
            adminUserDTO.setFirstname(user.getFirstName());
            adminUserDTO.setUserRoles(extractUserRoles(user.getUserRole()));
            adminUserDTO.setLastname(user.getLastName());
            adminUserDTO.setEnabled(user.isEnabled());

            return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(adminUserDTO, HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value="/role", method = RequestMethod.GET)
    public ResponseEntity<RoleListingDTO> getRoles(){
        List<RoleDTO> roleListDTO = new ArrayList<>();
        RoleListingDTO roleListingDTO = new RoleListingDTO();
        List<Role> roleList = userService.getRoles();
        for(Role role : roleList){
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setRoleId(role.getRoleID());
            roleDTO.setRole(role.getRole());

            roleListDTO.add(roleDTO);
        }
        roleListingDTO.setRoles(roleListDTO);
        return new ResponseEntity<>(roleListingDTO, HttpStatus.OK);
    }


    @RequestMapping(value="/role", method = RequestMethod.POST)
    public ResponseEntity<RoleDTO> addRole(@RequestParam String role){
        RoleDTO roleDTO = new RoleDTO();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Role newRole = new Role();
        newRole.setRole("ROLE_"+role.toUpperCase());
        newRole = userService.addRole(newRole);

        if(newRole != null) {
            roleDTO.setRoleId(newRole.getRoleID());
            roleDTO.setRole(newRole.getRole());
            return new ResponseEntity<>(roleDTO, HttpStatus.OK);
        }

        return new ResponseEntity<>(roleDTO, HttpStatus.BAD_REQUEST);
    }

    //
    @RequestMapping(value="/role", method = RequestMethod.PUT)
    public ResponseEntity<RoleDTO> updateRole(@RequestParam RoleDTO incomingRole){
        RoleDTO roleDTO = new RoleDTO();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Role role = userService.findRoleById(incomingRole.getRoleId());
        role.setRole(incomingRole.getRole());
        role = userService.updateRole(role);

        if(role != null){
            roleDTO.setRoleId(role.getRoleID());
            roleDTO.setRole(role.getRole());
            return new ResponseEntity<>(roleDTO, HttpStatus.OK);
        }

        return new ResponseEntity<>(roleDTO, HttpStatus.BAD_REQUEST);
    }

    private List<String> extractUserRoles(Set<UserRole> roles){
        List<String> rolesToAdd = new ArrayList<>();
        for (UserRole role : roles) {
            rolesToAdd.add(role.getRole().getRole());
        }
        return rolesToAdd;
    }
}