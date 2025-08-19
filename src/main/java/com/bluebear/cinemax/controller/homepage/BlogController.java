package com.bluebear.cinemax.controller.homepage;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.service.blog.BlogService;
import com.bluebear.cinemax.service.blogcategory.BlogCategoryService;
import com.bluebear.cinemax.service.bloglike.BlogLikeService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class BlogController {

    @Autowired
    private BlogService blogService;
    @Autowired
    private BlogCategoryService blogCategoryService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private BlogLikeService blogLikeService;

    private List<GenreDTO> genres;
    private Page<TheaterDTO> theaters;
    private Page<BlogDTO> blogs;
    private List<BlogCategoryDTO> categories;
    private List<BlogDTO> sameCateBlogs;
    private Map<String, LocalDateTime> viewTracking = new HashMap<>();

    @GetMapping("/blog")
    public String blog(Model model) {
        theaters = theaterService.getAllTheaters();
        genres = genreService.getAllGenres();
        categories = blogCategoryService.getAllCategories();
        blogs = blogService.getAllBlogs(PageRequest.of(0, Constant.BLOGS_PER_PAGE));

        model.addAttribute("categories", categories);
        model.addAttribute("blogs", blogs);
        model.addAttribute("genres", genres);
        model.addAttribute("theaters", theaters);
        model.addAttribute("currentWebPage", "blogs");
        return "customer/blog-list";
    }

    @GetMapping("/blog-detail")
    public String blogDetail(HttpSession session, Model model, @RequestParam(name = "blogId", required = false) Integer blogId, HttpServletRequest request) throws Exception {
        Boolean isLiked = false;
        BlogDTO blogDTO;
        if (blogId != null) {
            blogDTO = blogService.getBlogById(blogId);
        } else {
            blogDTO = blogService.findTop1Blog();
        }

        String userKey = request.getRemoteAddr();

        if (!viewedRecently(userKey, blogId)) {
            blogDTO.setViewCount(blogDTO.getViewCount() + 1);
            blogService.updateBlog(blogId, blogDTO);
            markAsViewed(userKey, blogId);
        }

        if (session.getAttribute("customer") != null) {
            CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
            isLiked = blogLikeService.hasLiked(blogDTO.getBlogID(), customerDTO.getId());
        }

        theaters = theaterService.getAllTheaters();
        genres = genreService.getAllGenres();
        sameCateBlogs = blogService.getBlogsWithSameCate(blogDTO);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("genres", genres);
        model.addAttribute("theaters", theaters);
        model.addAttribute("blog", blogDTO);
        model.addAttribute("sameCateBlogs", sameCateBlogs);
        model.addAttribute("currentWebPage", "blogs");
        return "customer/blog-detail";
    }

    @PostMapping("/blog-detail/like")
    public ResponseEntity<Void> likeBlog(@RequestParam(name = "blogId") Integer blogId, HttpSession session) {
        CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
        System.out.println("hehehehehe");


        if (blogLikeService.hasLiked(blogId, customerDTO.getId())) {
            blogLikeService.unlikeBlog(blogId, customerDTO.getId());
        } else {
            BlogLikeDTO blogLikeDTO = BlogLikeDTO.builder().customerID(customerDTO.getId()).blogID(blogId).likedAt(LocalDateTime.now()).build();
            blogLikeService.likeBlog(blogLikeDTO);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/load-blog")
    public String loadBlog(Model model, @RequestParam(name = "categoryId", required = false) Integer categoryId, @RequestParam(name = "title") String title, @RequestParam(name = "page") Integer page) {

        System.out.println("categoryId: " + categoryId);
        System.out.println("title: " + title);
        System.out.println("page: " + page);

        blogs = blogService.findBlogsFilter(categoryId, title, PageRequest.of(page - 1, Constant.BLOGS_PER_PAGE));
        model.addAttribute("blogs", blogs);
        return "customer/fragments/blog-list/list-blog :: list-blog";
    }

    private boolean viewedRecently(String userKey, Integer blogId) {
        String key = userKey + "_blog_" + blogId;
        LocalDateTime lastViewed = viewTracking.get(key);
        if (lastViewed == null) return false;
        return lastViewed.plusHours(1).isAfter(LocalDateTime.now());
    }

    private void markAsViewed(String userKey, Integer blogId) {
        String key = userKey + "_blog_" + blogId;
        viewTracking.put(key, LocalDateTime.now());
    }

}
