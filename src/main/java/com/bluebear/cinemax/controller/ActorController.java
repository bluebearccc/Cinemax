package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.ActorService;
import com.bluebear.cinemax.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/actors") // Đổi từ "/actors" thành "/admin/actors"
public class ActorController {

    @Autowired
    private ActorService actorService;

    @Autowired
    private MovieService movieService;

    // Trang danh sách diễn viên
    @GetMapping("")
    public String getAllActors(Model model) {
        List<ActorDTO> actors = actorService.getAllActors();

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", "Danh sách diễn viên");

        return "admin/list-actor"; // Đổi từ "actors/list" thành "admin/list-actor"
    }

    // Chi tiết diễn viên
    @GetMapping("/{id}")
    public String getActorDetail(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/admin/actors"; // Cập nhật redirect path
        }

        List<MovieDTO> movies = movieService.getMoviesByActor(id);

        model.addAttribute("actor", actor);
        model.addAttribute("movies", movies);

        return "admin/actor-detail"; // Đổi từ "actors/detail" thành "admin/actor-detail"
    }

    // Tìm kiếm diễn viên
    @GetMapping("/search")
    public String searchActors(@RequestParam(required = false) String keyword, Model model) {
        List<ActorDTO> actors;
        String pageTitle = "Kết quả tìm kiếm";

        if (keyword != null && !keyword.trim().isEmpty()) {
            actors = actorService.searchActorsByName(keyword);
            pageTitle = "Tìm kiếm diễn viên: " + keyword;
        } else {
            actors = actorService.getAllActors();
        }

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("keyword", keyword);

        return "admin/list-actor"; // Đổi từ "actors/list" thành "admin/list-actor"
    }

    // Thêm method để hiển thị form thêm actor mới
    @GetMapping("/add")
    public String addActorForm(Model model) {
        model.addAttribute("pageTitle", "Thêm diễn viên mới");
        return "admin/add-actor";
    }

    // Thêm method để xử lý thêm actor (có thể implement sau)
    @PostMapping("/add")
    public String addActor(@ModelAttribute ActorDTO actorDTO, Model model) {
        try {
            // Implement logic thêm actor
            // actorService.saveActor(actorDTO);
            model.addAttribute("success", "Thêm diễn viên thành công!");
            return "redirect:/admin/actors";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi thêm diễn viên!");
            return "admin/add-actor";
        }
    }
}