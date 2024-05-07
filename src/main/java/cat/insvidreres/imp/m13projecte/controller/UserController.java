package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.service.UserService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import com.google.protobuf.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*",
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT},
        allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/users")
    public JSONResponse saveUser(@RequestBody User user) throws ExecutionException, InterruptedException {

        return userService.saveUser(user);
    }

    @PostMapping("/users/login")
    public JSONResponse userLogin(@RequestHeader("idToken") String idToken, @RequestBody User user) throws ExecutionException, InterruptedException {

        return userService.login(idToken, user);
    }

    @GetMapping("/users")
    public JSONResponse getAllUsers(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.getUsers(idToken);
    }

    @GetMapping("/user/friends")
    public JSONResponse getUserFriends(@RequestHeader("idToken") String idToken, @RequestParam("email") String email) throws ExecutionException, InterruptedException {

        return userService.getUserDetails(idToken, email);
    }

    @DeleteMapping("/user/friends")
    public JSONResponse deleteFriendFromUser(@RequestHeader("idToken") String idToken, @RequestParam("email") String email, @RequestParam("friendEmail") String friendEmail) throws ExecutionException, InterruptedException {

        return userService.deleteUserFriend(idToken, email, friendEmail);
    }

    @PostMapping("/user/friends")
    public JSONResponse addFriendToUser(@RequestHeader("idToken") String idToken, @RequestParam("email") String email, @RequestBody User user) throws ExecutionException, InterruptedException {

        return userService.addUserFriend(idToken, email, user);
    }


    @GetMapping("/user")
    public JSONResponse getUser(@RequestHeader("idToken") String idToken, @RequestParam("email") String email) throws ExecutionException, InterruptedException {

        return userService.getUserDetails(idToken, email);
    }


    @PutMapping("/users")
    public JSONResponse updateUser(@RequestBody User user, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.updateUser(user, idToken);
    }

    @DeleteMapping("/users")
    public JSONResponse deleteUser(@RequestBody String email, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.deleteUser(email, idToken);
    }

    @PutMapping("/user/pfp")
    public JSONResponse updateUserPFP(@RequestHeader("idToken") String idToken, @RequestBody Map<String, Object> clientBody) throws ExecutionException, InterruptedException {

        return userService.updateUserPFP(idToken, clientBody);
    }
}
