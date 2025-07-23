package com.bluebear.cinemax.binder;

import com.bluebear.cinemax.dto.BlogCategoryDTO;
import com.bluebear.cinemax.service.blogcategory.BlogCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyEditorSupport;

@Component
public class CategoryEditor extends PropertyEditorSupport {

    @Autowired
    private BlogCategoryService blogCategoryService;

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            Integer id = Integer.parseInt(text);
            BlogCategoryDTO category = blogCategoryService.getCategoryById(id);
            setValue(category);
        } catch (NumberFormatException e) {
            setValue(null);
        }
    }
}

