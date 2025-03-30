package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.FeeCategoryService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/categories")
public class FeeCategoryAPI {
    @Autowired
    private FeeCategoryService feeCategoryService;

    // API lấy danh sách danh mục cha
    @GetMapping("/parent")
    public ResponseEntity<List<String>> getParentCategories() {
        List<String> categories = feeCategoryService.getParentCategoryNames();
        return ResponseEntity.ok(categories);
    }

    // API lấy danh sách danh mục con theo danh mục cha
    @GetMapping("/sub")
    public ResponseEntity<List<String>> getSubCategories(@RequestParam String parentCategory) {
        if (parentCategory == null || parentCategory.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        System.out.println(parentCategory);
        List<String> subCategories = feeCategoryService.getSubCategoriesNames(parentCategory);
        return ResponseEntity.ok(subCategories);
    }
}
