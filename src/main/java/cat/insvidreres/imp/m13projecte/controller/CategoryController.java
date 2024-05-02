package cat.insvidreres.imp.m13projecte.controller;


import cat.insvidreres.imp.m13projecte.service.CategoryService;
import cat.insvidreres.imp.m13projecte.utils.JSONResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api", produces = "application/json")
@CrossOrigin(origins = "*")

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public JSONResponse getAllCategories(@RequestHeader("idToken") String idToken) throws ExecutionException, InterruptedException {

        return categoryService.getAllCategories(idToken);
    }
}
