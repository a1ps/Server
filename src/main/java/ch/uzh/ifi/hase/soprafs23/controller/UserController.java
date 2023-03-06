package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @GetMapping("/users/{userId}") 
  @ResponseBody 
  public UserGetDTO getUser(@PathVariable Long userId) {
    System.out.println("User id: " + userId);
    // fetch user in the internal representation
    User user = userService.getUser(userId);
    System.out.println("User: " + user);
    // convert user to the API representation
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  //ai made this
  @PutMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userCredentials = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User user = userService.logInUser(userCredentials);
    // convert internal representation of user back to API
    System.out.println("User logged in: " + user.getUsername());
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PutMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO logoutUser(@RequestBody long id) {
    // convert API user to internal representation
    
    
    User user =userService.logoutUser(id);
    // convert internal representation of user back to API
    System.out.println("User logged out: " + user.getUsername());
    System.out.println("User status: " + user.getStatus()); 
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

}
