package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;
}
