package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.entities.Post;
import cat.insvidreres.imp.m13projecte.service.PostService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")

public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/posts")
    public JSONResponse savePost(@RequestBody Post post, @RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return postService.createPost(post, idToken);
    }

    @GetMapping("/allposts")
    public JSONResponse getAllPosts(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return postService.getAllPosts(idToken);
    }

    @GetMapping("/posts")
    public JSONResponse getPostsWithCategory(@RequestHeader("idToken") String idToken, @RequestHeader("Categories") String categories) throws ExecutionException, InterruptedException {

        //Categories concatenated like Category,Category,Category
        return postService.getPostsWithCategories(idToken, categories);
    }


    @GetMapping("/userPosts")
    public JSONResponse getUserPosts(@RequestHeader("idToken") String idToken, @RequestParam String email) throws ExecutionException, InterruptedException {

        return postService.getUserPosts(idToken, email);
    }





}