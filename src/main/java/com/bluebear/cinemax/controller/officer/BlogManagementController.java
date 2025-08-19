package com.bluebear.cinemax.controller.officer;

import com.bluebear.cinemax.binder.CategoryEditor;
import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.service.blog.BlogService;
import com.bluebear.cinemax.service.blogcategory.BlogCategoryService;
import com.bluebear.cinemax.service.bloglike.BlogLikeService;
import com.bluebear.cinemax.service.employee.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/officer/blog-management")
public class BlogManagementController {

    @Autowired
    private BlogCategoryService blogCategoryService;
    @Autowired
    private BlogService blogService;
    @Autowired
    private BlogLikeService blogLikeService;
    @Autowired
    private CategoryEditor categoryEditor;
    @Autowired
    private EmployeeService employeeService;

    private List<BlogCategoryDTO> categories;
    private Page<BlogDTO> blogs;
    private long totalLikes;
    private long totalViews;
    private long totalBlogs;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BlogCategoryDTO.class, categoryEditor);
    }

    @GetMapping
    public String blogManagement(Model model) {
        categories = blogCategoryService.getAllCategories();
        blogs = blogService.getAllBlogs(PageRequest.of(0, Constant.BLOG_PER_ADMIN_PAGE));
        totalViews = blogService.getTotalViewCount();
        totalBlogs = blogService.getTotalBlogCount();
        totalLikes = blogLikeService.getTotalBlogLikeCount();

        if (model.getAttribute("announce") != null) {
            model.addAttribute("announce", model.getAttribute("announce"));
        }

        model.addAttribute("totalLikes", totalLikes);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalBlogs", totalBlogs);
        model.addAttribute("categories", categories);
        model.addAttribute("currentWebPage", "blog-management");
        model.addAttribute("blogs", blogs);
        return "officer/blog-management";
    }

    @GetMapping("/add")
    public String blogAdd(Model model) {
        categories = blogCategoryService.getAllCategories();
        totalViews = blogService.getTotalViewCount();
        totalBlogs = blogService.getTotalBlogCount();
        model.addAttribute("blog", new BlogDTO());
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalBlogs", totalBlogs);
        model.addAttribute("categories", categories);
        model.addAttribute("currentWebPage", "blog-management");
        return "officer/blog-add";
    }

    @GetMapping("/edit/{id}")
    public String blogEdit(Model model, @PathVariable("id") Integer id) {
        BlogDTO blogDTO = blogService.getBlogById(id);
        categories = blogCategoryService.getAllCategories();
        totalViews = blogService.getTotalViewCount();
        totalBlogs = blogService.getTotalBlogCount();
        System.out.println(blogDTO.getSections().size());
        model.addAttribute("blog", blogDTO);
        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalBlogs", totalBlogs);
        model.addAttribute("categories", categories);
        model.addAttribute("currentWebPage", "blog-management");
        return "officer/blog-edit";
    }

    @PostMapping("/add-blog")
    public String blogAdd(@ModelAttribute("blog") BlogDTO blogDTO, RedirectAttributes redirectAttributes, HttpSession session, @RequestParam("blogImage")MultipartFile file) {
        blogDTO.setCreatedAt(LocalDateTime.now());
        blogDTO.setUpdatedAt(LocalDateTime.now());
        if (session.getAttribute("employee") != null) {
            EmployeeDTO employeeDTO = (EmployeeDTO) session.getAttribute("employee");
            blogDTO.setAuthorID(employeeDTO.getId());
        }
        blogDTO.setViewCount(0);
        blogDTO.setLikeCount(0);
        blogService.createBlog(blogDTO, file);
        blogs = blogService.getAllBlogs(PageRequest.of(0, Constant.BLOG_PER_ADMIN_PAGE));
        redirectAttributes.addFlashAttribute("blogs", blogs);
        redirectAttributes.addFlashAttribute("announce", "New blog has been added successfully !");
        return "redirect:/officer/blog-management";
    }

    @PostMapping("/edit-blog")
    public String blogEdit(@ModelAttribute("blog") BlogDTO blogDTO, RedirectAttributes redirectAttributes, @RequestParam("blogImage")MultipartFile file) {
        BlogDTO oldBlog = blogService.getBlogById(blogDTO.getBlogID());
        blogDTO.setUpdatedAt(LocalDateTime.now());
        blogDTO.setAuthorID(oldBlog.getAuthorID());
        blogDTO.setViewCount(oldBlog.getViewCount());
        blogDTO.setLikeCount(oldBlog.getLikeCount());
        blogService.updateBlog(blogDTO.getBlogID(), blogDTO, file);
        blogs = blogService.getAllBlogs(PageRequest.of(0, Constant.BLOG_PER_ADMIN_PAGE));
        redirectAttributes.addFlashAttribute("blogs", blogs);
        redirectAttributes.addFlashAttribute("announce", "Blog has been edited successfully !");
        return "redirect:/officer/blog-management";
    }

    @GetMapping("/delete/{id}")
    public String blogDelete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        blogService.deleteBlog(id);
        blogs = blogService.getAllBlogs(PageRequest.of(0, Constant.BLOG_PER_ADMIN_PAGE));
        redirectAttributes.addFlashAttribute("blogs", blogs);
        redirectAttributes.addFlashAttribute("announce", "Blog has been deleted successfully !");
        return "redirect:/officer/blog-management";
    }

    @GetMapping("/load-blog")
    public String loadBlog(Model model, @RequestParam(name = "categoryId", required = false) Integer categoryId, @RequestParam(name = "title") String title, @RequestParam(name = "page") Integer page) {
        blogs = blogService.findBlogsFilter(categoryId, title, PageRequest.of(page - 1, Constant.BLOG_PER_ADMIN_PAGE));
        model.addAttribute("blogs", blogs);
        return "officer/fragments/blog/list-blog :: list-blog";
    }

    @PostMapping("/add-category")
    public String categoryAdd(Model model, @RequestParam(name = "categoryName") String categoryName) {

        if (blogCategoryService.findCategoryByName(categoryName) != null) {
            categories = blogCategoryService.getAllCategories();
            model.addAttribute("error", "Duplicated category name !");
            model.addAttribute("categories", categories);
            return "officer/fragments/blog/list-category :: list-category";
        }

        BlogCategoryDTO blogCategoryDTO = BlogCategoryDTO.builder().categoryName(categoryName).build();
        blogCategoryService.createCategory(blogCategoryDTO);
        categories = blogCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("announce", "New category has been added successfully !");
        return "officer/fragments/blog/list-category :: list-category";
    }

    @PostMapping("/edit-category")
    public String categoryEdit(Model model, @RequestParam(name = "categoryId") Integer categoryId, @RequestParam(name = "categoryName") String categoryName) {
        blogCategoryService.updateCategory(categoryId, BlogCategoryDTO.builder().categoryName(categoryName).build());
        categories = blogCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("announce", "Category has been edited successfully !");
        return "officer/fragments/blog/list-category :: list-category";
    }

    @PostMapping("/delete-category")
    public String categoryDelete(Model model, @RequestParam(name = "categoryId") Integer categoryId) {
        blogCategoryService.deleteCategory(categoryId);
        categories = blogCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("announce", "Category has been deleted successfully !");
        return "officer/fragments/blog/list-category :: list-category";
    }

    @GetMapping("/reload-category-select")
    public String reloadCategorySelect(Model model) {
        model.addAttribute("categories", categories);
        return "officer/fragments/blog/category-select :: category-select";
    }

}
