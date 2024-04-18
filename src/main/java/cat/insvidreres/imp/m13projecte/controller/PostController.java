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
}