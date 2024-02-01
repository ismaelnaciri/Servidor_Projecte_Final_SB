package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public String saveUser(@RequestBody User user) throws ExecutionException, InterruptedException {

        return userService.saveUser(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {

        return userService.getUsers();
    }

    @GetMapping("/user/{docName}")
    public User getUser(@PathVariable String docName) throws ExecutionException, InterruptedException {

        return userService.getUserDetails(docName);
    }

    //Change to "/user/{firstName}"
    @PutMapping("/users")
    public String updateUser(@RequestBody User user) throws ExecutionException, InterruptedException {

        return userService.updateUser(user);
    }

    @DeleteMapping("/users")
    public String deleteUser(@RequestBody String docName) throws ExecutionException, InterruptedException {

        return userService.deleteUser(docName);
    }
}
