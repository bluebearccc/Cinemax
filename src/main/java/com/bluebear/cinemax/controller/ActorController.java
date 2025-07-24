package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.ActorService;
import com.bluebear.cinemax.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/actors")
public class ActorController {

    @Autowired
    private ActorService actorService;

    @Autowired
    private MovieService movieService;

    // Trang danh sách diễn viên
    @GetMapping("")
    public String getAllActors(Model model) {
        List<ActorDTO> actors = actorService.getAllActors();

        // Sắp xếp theo ID tăng dần
        if (actors != null) {
            actors.sort(Comparator.comparing(ActorDTO::getActorID, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", "Danh sách diễn viên");

        return "admin/list-actor";
    }

    // Chi tiết diễn viên
    @GetMapping("/{id}")
    public String getActorDetail(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/admin/actors";
        }

        List<MovieDTO> movies = movieService.getMoviesByActor(id);

        model.addAttribute("actor", actor);
        model.addAttribute("movies", movies);
        model.addAttribute("pageTitle", "Chi tiết diễn viên - " + actor.getActorName());

        return "admin/detail-actor";
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

        // Sắp xếp theo ID tăng dần
        if (actors != null) {
            actors.sort(Comparator.comparing(ActorDTO::getActorID, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        model.addAttribute("actors", actors);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("keyword", keyword);

        return "admin/list-actor";
    }

    // Hiển thị form thêm actor mới - CHUYỂN VỀ FORM-ACTOR
    @GetMapping("/add")
    public String addActorForm(Model model) {
        ActorDTO actor = new ActorDTO(); // Tạo object rỗng cho form

        // Lấy tất cả phim từ database
        List<MovieDTO> allMovies = movieService.getAllMovies();

        // DEBUG: Kiểm tra dữ liệu phim
        System.out.println("DEBUG - Loading " + (allMovies != null ? allMovies.size() : 0) + " movies for actor form");
        if (allMovies != null && !allMovies.isEmpty()) {
            System.out.println("DEBUG - First movie: " + allMovies.get(0).getMovieName());
        }

        model.addAttribute("actor", actor);
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm diễn viên mới");

        return "admin/form-actor"; // Chuyển về form-actor cho ADD
    }

    // Xử lý thêm actor mới
    @PostMapping("/add")
    public String addActor(@ModelAttribute ActorDTO actorDTO,
                           @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // Validate và xử lý đường dẫn ảnh
            String imagePath = actorDTO.getImage();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // Clean up image path
                imagePath = imagePath.trim();
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                actorDTO.setImage(imagePath);

                // Log for debugging
                System.out.println("DEBUG - Actor image path set to: " + imagePath);
            } else {
                // Use default image if no path provided
                actorDTO.setImage("/images/default-actor.png");
                System.out.println("DEBUG - Using default actor image");
            }

            // Lưu actor trước
            ActorDTO savedActor = actorService.saveActor(actorDTO);

            // Xử lý danh sách phim đã chọn
            if (selectedMovies != null && !selectedMovies.trim().isEmpty()) {
                List<Integer> movieIds = Arrays.stream(selectedMovies.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                // Cập nhật quan hệ Actor-Movie
                actorService.updateActorMovies(savedActor.getActorID(), movieIds);

                System.out.println("DEBUG - Actor assigned to " + movieIds.size() + " movies");
            }

            redirectAttributes.addFlashAttribute("success", "Thêm diễn viên thành công!");
            return "redirect:/admin/actors";

        } catch (Exception e) {
            // Lấy lại danh sách phim khi có lỗi
            List<MovieDTO> allMovies = movieService.getAllMovies();

            model.addAttribute("actor", actorDTO);
            model.addAttribute("allMovies", allMovies);
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Thêm diễn viên mới");
            model.addAttribute("error", "Có lỗi xảy ra khi thêm diễn viên: " + e.getMessage());

            System.err.println("ERROR - Adding actor: " + e.getMessage());
            e.printStackTrace();

            return "admin/form-actor"; // Quay lại form-actor khi có lỗi
        }
    }

    // Hiển thị form chỉnh sửa actor - CHUYỂN VỀ EDIT-ACTOR
    @GetMapping("/edit/{id}")
    public String editActorForm(@PathVariable Integer id, Model model) {
        ActorDTO actor = actorService.getActorById(id);
        if (actor == null) {
            return "redirect:/admin/actors";
        }

        // Lấy tất cả phim và phim hiện tại của actor
        List<MovieDTO> allMovies = movieService.getAllMovies();
        List<MovieDTO> actorMovies = movieService.getMoviesByActor(id);

        // DEBUG: Kiểm tra dữ liệu
        System.out.println("DEBUG - Edit form: " + (allMovies != null ? allMovies.size() : 0) + " total movies");
        System.out.println("DEBUG - Actor movies: " + (actorMovies != null ? actorMovies.size() : 0) + " movies");
        System.out.println("DEBUG - Actor current image: " + actor.getImage());

        // Tạo danh sách ID phim hiện tại
        List<Integer> currentMovieIds = actorMovies.stream()
                .map(MovieDTO::getMovieID)
                .collect(Collectors.toList());

        model.addAttribute("actor", actor);
        model.addAttribute("allMovies", allMovies);
        model.addAttribute("currentMovieIds", currentMovieIds);
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Chỉnh sửa diễn viên - " + actor.getActorName());

        return "admin/edit-actor"; // Chuyển về edit-actor cho EDIT
    }

    // Xử lý cập nhật actor
    @PostMapping("/edit/{id}")
    public String updateActor(@PathVariable Integer id,
                              @ModelAttribute ActorDTO actorDTO,
                              @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            // Đảm bảo ID được set đúng
            actorDTO.setActorID(id);

            // Validate tên diễn viên
            if (actorDTO.getActorName() == null || actorDTO.getActorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên diễn viên không được để trống");
            }

            // Validate và xử lý đường dẫn ảnh
            String imagePath = actorDTO.getImage();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // Clean up image path
                imagePath = imagePath.trim();
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                actorDTO.setImage(imagePath);

                // Log for debugging
                System.out.println("DEBUG - Actor image path updated to: " + imagePath);
            }
            // If imagePath is null or empty, the service will handle keeping existing or using default

            // Cập nhật thông tin actor
            ActorDTO updatedActor = actorService.updateActor(actorDTO);

            // Xử lý cập nhật danh sách phim
            List<Integer> movieIds = null;
            if (selectedMovies != null && !selectedMovies.trim().isEmpty()) {
                movieIds = Arrays.stream(selectedMovies.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                System.out.println("DEBUG - Updating actor with " + movieIds.size() + " movies");
            } else {
                System.out.println("DEBUG - Removing actor from all movies");
            }

            // Cập nhật quan hệ Actor-Movie (có thể là danh sách rỗng để xóa tất cả)
            actorService.updateActorMovies(id, movieIds != null ? movieIds : List.of());

            redirectAttributes.addFlashAttribute("success", "Cập nhật diễn viên thành công!");
            return "redirect:/admin/actors/" + id;

        } catch (Exception e) {
            // Lấy lại dữ liệu khi có lỗi
            List<MovieDTO> allMovies = movieService.getAllMovies();
            List<MovieDTO> actorMovies = movieService.getMoviesByActor(id);
            List<Integer> currentMovieIds = actorMovies.stream()
                    .map(MovieDTO::getMovieID)
                    .collect(Collectors.toList());

            model.addAttribute("actor", actorDTO);
            model.addAttribute("allMovies", allMovies);
            model.addAttribute("currentMovieIds", currentMovieIds);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Chỉnh sửa diễn viên - " + actorDTO.getActorName());
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật diễn viên: " + e.getMessage());

            System.err.println("ERROR - Updating actor: " + e.getMessage());
            e.printStackTrace();

            return "admin/edit-actor"; // Quay lại edit-actor khi có lỗi
        }
    }

    // Xóa actor
    @PostMapping("/delete/{id}")
    public String deleteActor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ActorDTO actor = actorService.getActorById(id);
            if (actor == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy diễn viên!");
                return "redirect:/admin/actors";
            }

            // Kiểm tra xem diễn viên có đang tham gia phim nào không
            List<MovieDTO> movies = movieService.getMoviesByActor(id);
            if (movies != null && !movies.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể xóa diễn viên đang tham gia " + movies.size() + " phim!");
                return "redirect:/admin/actors/" + id;
            }

            actorService.deleteActor(id);

            System.out.println("DEBUG - Actor deleted successfully: " + actor.getActorName());
            redirectAttributes.addFlashAttribute("success", "Xóa diễn viên thành công!");
            return "redirect:/admin/actors";
        } catch (Exception e) {
            System.err.println("ERROR - Deleting actor: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa diễn viên: " + e.getMessage());
            return "redirect:/admin/actors/" + id;
        }
    }
}