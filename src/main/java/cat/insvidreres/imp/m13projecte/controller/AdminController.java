package cat.insvidreres.imp.m13projecte.controller;

import cat.insvidreres.imp.m13projecte.entities.User;
import cat.insvidreres.imp.m13projecte.service.AdminService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/admin/login")
    public JSONResponse login(@RequestHeader("idToken") String idToken) {
        return adminService.login(idToken);
    }

    @DeleteMapping("/admin/deletePost/{idPost}")
    public JSONResponse deletePost(@RequestHeader("idToken") String idToken, @PathVariable String idPost) {
        //Call adminService
        return adminService.deletePost(idPost, idPost);
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

    @DeleteMapping("/admin/deleteUser")
    public JSONResponse deleteUser(@RequestHeader("idToken") String idToken, @RequestBody User user) {

        return adminService.deleteUser(idToken, user);
    }
}
