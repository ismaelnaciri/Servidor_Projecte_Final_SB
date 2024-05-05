package cat.insvidreres.imp.m13projecte.controller;

import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.service.AdminService;
import cat.insvidreres.imp.m13projecte.service.PostService;
import cat.insvidreres.imp.m13projecte.service.UserService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/admin/login")
    public JSONResponse login(@RequestHeader("idToken") String idToken, @RequestBody User user) {

        return adminService.login(idToken, user);
    }

    @DeleteMapping("/admin/deletePost/{idPost}")
    public JSONResponse deletePost(@RequestHeader("idToken") String idToken, @PathVariable String idPost) {

        return adminService.deletePost(idToken, idPost);
    }

    @PostMapping("/admin/insertCategory")
    public JSONResponse insertCategory(@RequestHeader("idToken") String idToken, @RequestBody String category) {

        return adminService.addCategory(idToken, category);
    }

    @DeleteMapping("/admin/deleteCategory")
    public JSONResponse deleteCategory(@RequestHeader("idToken") String idToken, @RequestBody String category) {

        return adminService.deleteCategory(idToken, category);
    }


    @GetMapping("/admin/getUsers")
    public JSONResponse getUsers(@RequestHeader("idToken") String idToken) {

        return adminService.adminGetAllAuthUsers(idToken);
    }

    @DeleteMapping("/admin/deleteUser/{userId}")
    public JSONResponse deleteUser(@RequestHeader("idToken") String idToken, @PathVariable("userId") String userId) {

        return adminService.deleteUser(idToken, userId);
    }

    @DeleteMapping("/admin/deletePostComment/{idPost}/{idComment}")
    public JSONResponse deletePostComment(@RequestHeader("idToken") String idToken, @PathVariable String idPost, @PathVariable String idComment) throws ExecutionException, InterruptedException {

        return adminService.deletePostComment(idToken, idPost, idComment);
    }

}
