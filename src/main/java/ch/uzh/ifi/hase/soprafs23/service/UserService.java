package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User getUser(long userId) {
    return this.userRepository.findById(userId);
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate(LocalDate.now());
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

	
  public User logInUser(User user) {
    User userToBeLoggedIn = userRepository.findByUsername(user.getUsername());
    if (userToBeLoggedIn == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The user with the given username does not exist!");
    }
    if (!userToBeLoggedIn.getName().equals(user.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is incorrect!");
    }
    userToBeLoggedIn.setStatus(UserStatus.ONLINE);
    userToBeLoggedIn = userRepository.save(userToBeLoggedIn);
    userRepository.flush();
    log.debug("Logged in User: {}", userToBeLoggedIn);
    return userToBeLoggedIn; 
  }

  public User logoutUser(long id){
    User user = userRepository.findById(id);
    user.setStatus(UserStatus.OFFLINE);
    user = userRepository.save(user);
    userRepository.flush();
    log.debug("Logged out User: {}", user);
    return user;
  }

  public User editUser(User user, UserPostDTO userChanges) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDate birthDate = LocalDate.parse(userChanges.getBirthDate());
    birthDate.format(formatter);
    user.setBirthDate(birthDate);
    user.setUsername(userChanges.getUsername());
    user = userRepository.save(user);
    userRepository.flush();
    return user;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    User userByName = userRepository.findByName(userToBeCreated.getName());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null && userByName != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(baseErrorMessage, "username and the name", "are"));
    } else if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    } else if (userByName != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "name", "is"));
    }
  }
}
