package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;

import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }
  
  //valid post test
  @Test
  public void createUser_validPOST_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }


  //self made test below
  //invalid post test
  @Test
  public void createUser_invalidPOST_noUserCreated() throws Exception {
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    ResponseStatusException conflict = new ResponseStatusException(HttpStatus.CONFLICT, "Your chosen credentials already exists");
    
    given(userService.createUser(Mockito.any())).willThrow(conflict);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(postRequest)
        .andExpect(status().isConflict())
        .andExpect(status().reason("Your chosen credentials already exists"));
  }

  //valid get test
  @Test
  public void getUser_validGET_userReturned() throws Exception {
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.getUser(user.getId())).willReturn(user);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest = get("/users/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    
  }

  //invalid get test
  @Test
  public void getUser_invalidGET_ErrosReturned() throws Exception {
    //query id that does not correspond to any user
    long fakeId = 2L;

    ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    
    given(userService.getUser(fakeId)).willThrow(notFound);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest = get("/users/" + fakeId);

    //then
    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound())
        .andExpect(status().reason("User not found"));
  }

  //valid put test
  @Test
  public void updateUser_validPUT_userUpdated() throws Exception {
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    LocalDate birthDate = LocalDate.of(1999, 2, 3);
    user.setBirthDate(birthDate);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("Test User edited");
    userPostDTO.setBirthDate("1999 03 03");

    //given(userService.editUser(user, userPostDTO)).willReturn(user);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/" + user.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(putRequest)
        .andExpect(status().isNoContent());
        
  }

  //invalid put test
  @Test
  public void updateUser_invalidPUT_ErrosReturned() throws Exception {
    //query id that does not correspond to any user
    long fakeId = 2L;

    //from the user requested changes
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("Test User edited");
    userPostDTO.setBirthDate("1999 03 03");

    ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    
    given(userService.getUser(fakeId)).willThrow(notFound);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/" + fakeId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(putRequest)
        .andExpect(status().isNotFound())
        .andExpect(status().reason("User not found"));
  }

  //test for login/logout 
  
  //valid login test
  @Test
  public void loginUser_validPOST_userLoggedIn() throws Exception {
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.logInUser(Mockito.any())).willReturn(user);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  //invalid login test with wrong credentials
  @Test
  public void loginUser_invalidPOST_ErrosReturned() throws Exception {
    //login with a userName that does not exist
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("Test User ");
    userPostDTO.setName("testUsername");

    ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    
    given(userService.logInUser(Mockito.any())).willThrow(notFound);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    //then
    mockMvc.perform(postRequest)
        .andExpect(status().isNotFound())
        .andExpect(status().reason("User not found"));

  }  


  //login with the wrong name
  @Test
  public void loginUser_invalidNamePOST_ErrosReturned() throws Exception{ 
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTOWrongName = new UserPostDTO();
    userPostDTOWrongName.setUsername("Test User");
    userPostDTOWrongName.setName("wrongtestUsername");

    ResponseStatusException unauthorized = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your Name is incorrect");
    
    given(userService.logInUser(Mockito.any())).willThrow(unauthorized);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequestWrongName = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTOWrongName));

    //then
    mockMvc.perform(postRequestWrongName)
        .andExpect(status().isUnauthorized())
        .andExpect(status().reason("Your Name is incorrect"));
  }

  //valid logout test
  @Test
  public void logoutUser_validPUT_userLoggedOut() throws Exception {
    //given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.OFFLINE);

    long userId = 1L;

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = put("/logout")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userId));

    //then
    mockMvc.perform(postRequest)
        .andExpect(status().isNoContent());
  }

  //invalid logout test
  @Test
  public void logoutUser_invalidPUT_ErrosReturned() throws Exception {
    //given
    long fakeId = 60L;

    ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    
    given(userService.logoutUser(fakeId)).willThrow(notFound);

    //when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = put("/logout")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(fakeId));

    //then
    mockMvc.perform(postRequest)
        .andExpect(status().isNotFound())
        .andExpect(status().reason("User not found"));
  }





  

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}