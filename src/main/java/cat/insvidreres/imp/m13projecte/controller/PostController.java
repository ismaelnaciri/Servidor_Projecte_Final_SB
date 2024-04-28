package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.entities.Comment;
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
    public JSONResponse savePost(@RequestBody Post post, @RequestHeader("idToken") String idToken, @RequestParam("category") String category) throws ExecutionException, InterruptedException {
        // Asegúrate de que la categoría se establezca en el objeto Post antes de llamar a createPost
        post.getCategories().add(category); // Agregar la categoría seleccionada
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

    @PostMapping("/postcomment")
    public JSONResponse addPostComment(@RequestBody Comment comment,  @RequestHeader("idToken") String idToken, @RequestParam ("idPost") String idPost)  throws ExecutionException, InterruptedException {

        return postService.addCommentPost(comment, idToken, idPost);
    }

    @PostMapping("/addLikePost")
    public JSONResponse addLikePost(  @RequestHeader("idToken") String idToken, @RequestParam ("idPost") String idPost, @RequestBody String email)  throws ExecutionException, InterruptedException {

        return postService.addLikePost(idToken, idPost, email);
    }

    @PostMapping("/deleteLikePost")
    public JSONResponse deleteLikePost( @RequestHeader("idToken") String idToken, @RequestParam ("idPost") String idPost, @RequestBody String email)  throws ExecutionException, InterruptedException {

        return postService.deleteLikePost( idToken, idPost,email);
    }

    @PostMapping("/addLikePostComment")
    public JSONResponse addLikeComment(  @RequestHeader("idToken") String idToken, @RequestParam ("idPost") String idPost, @RequestParam ("idComment") String idComment, @RequestBody String email)  throws ExecutionException, InterruptedException {

        return postService.addLikeCommentPost(idToken, idPost, idComment, email);
    }

    @PostMapping("/deleteLikePostComment")
    public JSONResponse deleteLikeComment( @RequestHeader("idToken") String idToken, @RequestParam ("idPost") String idPost, @RequestParam ("idComment") String idComment, @RequestBody String email)  throws ExecutionException, InterruptedException {

        return postService.deleteLikeCommentPost( idToken, idPost, idComment, email);
    }

    @GetMapping("/categories")
    public JSONResponse getCategories(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {
        return postService.getCategories(idToken);
    }




}