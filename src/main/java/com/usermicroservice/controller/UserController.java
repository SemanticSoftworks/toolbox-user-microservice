package com.usermicroservice.controller;

import com.usermicroservice.Hash;
import com.usermicroservice.domain.Role;
import com.usermicroservice.domain.User;
import com.usermicroservice.domain.UserRole;
import com.usermicroservice.model.*;
import com.usermicroservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dani on 2017-02-22.
 */
@RestController
@RequestMapping("/user")
public class UserController{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<Long> getPermission(@RequestParam String username, @RequestParam String password){
        logger.info("incoming user with username: "+username + " and password: "+password+" is asking for permission");
        User userCheck = userService.findByUserNameAndPassword(username, password);

        if(userCheck != null){
            if(checkAdminRole(userCheck.getUserRole())) {
                return new ResponseEntity<>(userCheck.getId(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(0L, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id){
        User user = userService.findUserById(id);
        UserDTO userDTO = new UserDTO();
        logger.info("ID is: "+id);
        if(user != null){
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setLastname(user.getLastName());
            userDTO.setFirstname(user.getFirstName());
            userDTO.setEmail(user.getEmail());
            userDTO.setUserRoles(extractUserRoles(user.getUserRole()));
        }

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<UserDTO> login(@RequestBody UserAuthenticationDTO incomingUser){
        logger.info("THIS says: username: "+incomingUser.getUsername() + " password: "+incomingUser.getPassword());
        User tmpUser = userService.findByUserNameAndPassword(incomingUser.getUsername(), incomingUser.getPassword());
        UserDTO userToReturn = new UserDTO();

        if(tmpUser != null && tmpUser.isEnabled()){
            userToReturn.setId(tmpUser.getId());
            userToReturn.setUsername(tmpUser.getUsername());
            userToReturn.setEmail(tmpUser.getEmail());
            userToReturn.setFirstname(tmpUser.getFirstName());
            userToReturn.setLastname(tmpUser.getLastName());

            userToReturn.setUserRoles(extractUserRoles(tmpUser.getUserRole()));
            return new ResponseEntity<>(userToReturn, HttpStatus.OK);
        }
        return new ResponseEntity<>(userToReturn, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/register", method = RequestMethod.POST, consumes={"application/json"})
    public ResponseEntity<UserDTO> register(@RequestBody UserRegistrationDTO incomingUser){
        logger.info("username of incoming user: "+incomingUser.getUsername());
        User tmpUser = userService.findByUserNameAndPassword(incomingUser.getUsername(), incomingUser.getPassword());
        UserDTO userDTO = new UserDTO();

        if(tmpUser == null){
            User newUser = new User();
            newUser.setUsername(incomingUser.getUsername());
            newUser.setPassword(Hash.BcryptEncrypt(incomingUser.getPassword()));
            newUser.setEmail(incomingUser.getEmail());
            newUser.setEnabled(true);
            newUser.setFirstName(incomingUser.getFirstname());
            newUser.setLastName(incomingUser.getLastname());

            User mockUser = userService.addUser(newUser);

            if(mockUser != null){
                UserRole newUserRole = new UserRole();
                newUserRole.setUser(mockUser);
                newUserRole.setRole(userService.getRole("ROLE_AUCTIONEER"));
                userService.addUserRole(newUserRole);

                userDTO.setId(mockUser.getId());
                userDTO.setUsername(mockUser.getUsername());
                userDTO.setEmail(mockUser.getEmail());
                userDTO.setFirstname(mockUser.getFirstName());
                userDTO.setLastname(mockUser.getLastName());
                userDTO.setUserRoles(extractUserRoles(mockUser.getUserRole()));
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    // cant change username & disable!
    @RequestMapping(value="/update" , method = RequestMethod.POST, consumes={"application/json"})
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserRegistrationDTO incomingUser){
        UserDTO userDTO = new UserDTO();

        User userToUpdate = userService.findByUsername(incomingUser.getUsername());
        userToUpdate.setPassword(Hash.BcryptEncrypt(incomingUser.getPassword()));
        userToUpdate.setFirstName(incomingUser.getFirstname());
        userToUpdate.setLastName(incomingUser.getLastname());
        userToUpdate.setEmail(incomingUser.getEmail());

        User userReturned = userService.updateUser(userToUpdate);
        if(userReturned != null){
            userDTO.setId(userReturned.getId());
            userDTO.setUsername(userReturned.getUsername());
            userDTO.setEmail(userReturned.getEmail());
            userDTO.setUserRoles(extractUserRoles(userReturned.getUserRole()));
            userDTO.setFirstname(userReturned.getFirstName());
            userDTO.setLastname(userReturned.getLastName());

            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/forgotpassword", method = RequestMethod.POST)
    public ResponseEntity<UserUpdateDTO> changePassword(@RequestParam String username, @RequestParam String email , @RequestParam String newPassword){
        UserUpdateDTO adminUserDTO= new UserUpdateDTO();

        User user = userService.findByUsername(username);
        user.setPassword(Hash.BcryptEncrypt(newPassword));
        user = userService.updateUser(user);

        if(user != null && user.getEmail().equals(email)){
            adminUserDTO.setId(user.getId());
            adminUserDTO.setUsername(user.getUsername());
            adminUserDTO.setEmail(user.getEmail());
            adminUserDTO.setFirstname(user.getFirstName());
            adminUserDTO.setUserRoles(extractUserRoles(user.getUserRole()));
            adminUserDTO.setLastname(user.getLastName());
            adminUserDTO.setPassword(newPassword);

            return new ResponseEntity<>(adminUserDTO, HttpStatus.OK);
        }

        return new ResponseEntity<>(adminUserDTO, HttpStatus.BAD_REQUEST);
    }

    // ADMIN STUFF --> need to check role of the user!
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ResponseEntity<List<AdminUserDTO>> AdmingetUsers(@RequestBody UserAuthenticationDTO userAuthenticationDTO ,@RequestParam Long startPosition, @RequestParam Long endPosition){
        List<AdminUserDTO> userDTOList = new ArrayList<>();
        User userCheck = userService.findByUserNameAndPassword(userAuthenticationDTO.getUsername(), userAuthenticationDTO.getPassword());

        if(userCheck != null) {
            if(checkAdminRole(userCheck.getUserRole())){
                List<User> userList = userService.findAllUsers(startPosition, endPosition);

                for (User user : userList) {
                    AdminUserDTO userDTO = new AdminUserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setUsername(user.getUsername());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setFirstname(user.getFirstName());
                    userDTO.setLastname(user.getLastName());
                    userDTO.setUserRoles(extractUserRoles(user.getUserRole()));
                    userDTO.setEnabled(user.isEnabled());
                    userDTO.setPassword(user.getPassword());

                    userDTOList.add(userDTO);
                }
                return new ResponseEntity<>(userDTOList, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(userDTOList, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/admin", method = RequestMethod.POST, consumes={"application/json"})
    public ResponseEntity<AdminUserDTO> Adminregister(@RequestBody UserAuthenticationDTO userAuthenticationDTO ,@RequestBody AdminUserAdderDTO incomingUser){
        AdminUserDTO userDTO = new AdminUserDTO();
        User userCheck = userService.findByUserNameAndPassword(userAuthenticationDTO.getUsername(), userAuthenticationDTO.getPassword());

        if(userCheck != null) {
            if(checkAdminRole(userCheck.getUserRole())) {
                User userToAdd = userService.findByUsername(incomingUser.getUsername());
                if (userToAdd == null){
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
            }
        }
        return new ResponseEntity<>(userDTO, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/admin/accountActivation/{id}", method = RequestMethod.POST)
    public ResponseEntity<AdminUserDTO> AdminaccountActivation(@PathVariable Long id , @RequestParam Boolean enable, @RequestParam UserAuthenticationDTO userAuthenticationDTO){
        AdminUserDTO userToReturn = new AdminUserDTO();

        User userChecker = userService.findByUserNameAndPassword(userAuthenticationDTO.getUsername(), userAuthenticationDTO.getPassword());

        if(userChecker != null) {
            if(checkAdminRole(userChecker.getUserRole())) {
                User user = userService.findUserById(id);
                user.setEnabled(enable);
                user = userService.updateUser(user);

                userToReturn.setId(user.getId());
                userToReturn.setUserRoles(extractUserRoles(user.getUserRole()));
                userToReturn.setUsername(user.getUsername());
                userToReturn.setEmail(user.getEmail());
                userToReturn.setFirstname(user.getFirstName());
                userToReturn.setLastname(user.getLastName());
                userToReturn.setEnabled(user.isEnabled());
            }
            return new ResponseEntity<>(userToReturn, HttpStatus.OK);
        }
        return new ResponseEntity<>(userToReturn, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/admin/update", method = RequestMethod.POST, consumes={"application/json"})
    public ResponseEntity<AdminUserDTO> AdminupdateUser(@RequestParam UserAuthenticationDTO userAuthenticationDTO ,@RequestBody AdminUserDTO incomingUser){

        AdminUserDTO adminUserDTO= new AdminUserDTO();
        Set<UserRole> userRoles = new HashSet<>();

        User userCheck = userService.findByUserNameAndPassword(userAuthenticationDTO.getUsername(),userAuthenticationDTO.getPassword());
        if(userCheck != null) {
            if(checkAdminRole(userCheck.getUserRole())) {
                User user = userService.findUserById(incomingUser.getId());
                user.setPassword(Hash.BcryptEncrypt(incomingUser.getPassword()));
                user.setEmail(incomingUser.getEmail());
                user.setFirstName(incomingUser.getFirstname());
                user.setLastName(incomingUser.getLastname());
                user.setEnabled(incomingUser.isEnabled());
                for (String userRole : incomingUser.getUserRoles()) {
                    UserRole realUserRole = new UserRole();
                    realUserRole.setUser(user);

                    Role roleCheck = userService.getRole(userRole);
                    if (roleCheck != null) {
                        realUserRole.setRole(roleCheck);
                    }
                    realUserRole = userService.addUserRole(realUserRole);
                    userRoles.add(realUserRole);
                }
                user.setUserRole(userRoles);
                user.setUserRole(userRoles);

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
            }
        }
        return new ResponseEntity<>(adminUserDTO, HttpStatus.BAD_REQUEST);
    }

    private List<String> extractUserRoles(Set<UserRole> roles){
        List<String> rolesToAdd = new ArrayList<>();
        for (UserRole role : roles) {
            rolesToAdd.add(role.getRole().getRole());
        }
        return rolesToAdd;
    }

    private boolean checkAdminRole(Set<UserRole> userRoles){
        for(UserRole role : userRoles){
            if(role.getRole().getRole().equals("ROLE_ADMIN")){
                return true;
            }
        }
        return false;
    }
}
