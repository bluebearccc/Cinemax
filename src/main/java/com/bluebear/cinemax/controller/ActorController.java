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
@RequestMapping("/actors")
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

        return "actors/list";
    }

    // Chi tiết diễn viên
    @GetMapping("/{id}")
    public String getActorDetail(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/actors";
        }

        List<MovieDTO> movies = movieService.getMoviesByActor(id);

        model.addAttribute("actor", actor);
        model.addAttribute("movies", movies);

        return "actors/detail";
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

        return "actors/list";
    }
}