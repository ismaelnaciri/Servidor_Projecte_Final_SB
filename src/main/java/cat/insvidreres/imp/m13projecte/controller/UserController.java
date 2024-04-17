package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.service.UserService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
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
    public JSONResponse userLogin(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.login(idToken);
    }

    @GetMapping("/users")
    public JSONResponse getAllUsers(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.getUsers(idToken);
    }

//    @GetMapping("/test/user/{email}/{password}")
//    public JSONResponse getUserWithHash(@PathVariable String email, @PathVariable String password) throws ExecutionException, InterruptedException, NoSuchAlgorithmException {
//
//        return userService.testSaltHashGet(email, password);
//    }


    @GetMapping("/user/{email}")
    public JSONResponse getUser(@PathVariable String email, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.getUserDetails(email, idToken);
    }

    //Change to "/user/{firstName}"
    @PutMapping("/users")
    public JSONResponse updateUser(@RequestBody User user, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.updateUser(user, idToken);
    }

    @DeleteMapping("/users")
    public JSONResponse deleteUser(@RequestBody String email, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return userService.deleteUser(email, idToken);
    }
}
