package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.service.GenreService;
import com.bluebear.cinemax.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @Autowired
    private MovieService movieService;

    // Hiển thị danh sách tất cả thể loại với movie count
    @GetMapping
    public String listGenres(
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            System.out.println("=== GENRE CONTROLLER START ===");
            List<GenreDTO> genres;
            Map<Integer, Long> movieCounts;

            // Tìm kiếm hoặc lấy tất cả
            if (keyword != null && !keyword.trim().isEmpty()) {
                System.out.println("Searching for keyword: " + keyword);
                genres = genreService.searchGenres(keyword);
                movieCounts = genreService.getGenreMovieCountsByKeyword(keyword);
                model.addAttribute("keyword", keyword);
            } else {
                System.out.println("Getting all genres");
                genres = genreService.getAllGenres();
                movieCounts = genreService.getAllGenreMovieCounts();
            }

            System.out.println("Found " + genres.size() + " genres");
            model.addAttribute("genres", genres);
            model.addAttribute("movieCounts", movieCounts);

            // Statistics
            System.out.println("Getting total genres count");
            long totalGenres = genreService.getTotalGenres();
            model.addAttribute("totalGenres", totalGenres);

            long genresWithMovies = genreService.getGenresWithMoviesCount();
            model.addAttribute("genresWithMovies", genresWithMovies);

            GenreDTO mostPopularGenre = genreService.getMostPopularGenre();
            model.addAttribute("mostPopularGenre", mostPopularGenre);
            model.addAttribute("mostPopularGenreName", mostPopularGenre != null ? mostPopularGenre.getGenreName() : "N/A");

            model.addAttribute("pageTitle", "Genre Management");

            System.out.println("=== GENRE CONTROLLER SUCCESS ===");

        } catch (Exception e) {
            System.err.println("=== GENRE CONTROLLER ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Lỗi khi tải danh sách thể loại: " + e.getMessage());
            model.addAttribute("genres", List.of());
            model.addAttribute("movieCounts", Map.of());

            // Set safe default values
            model.addAttribute("totalGenres", 0L);
            model.addAttribute("genresWithMovies", 0L);
            model.addAttribute("mostPopularGenre", null);
            model.addAttribute("mostPopularGenreName", "N/A");
            model.addAttribute("activeLink", "genres");
        }

        return "/admin/list-genre";
    }

    // Tìm kiếm thể loại
    @GetMapping("/search")
    public String searchGenres(
            @RequestParam String keyword,
            Model model) {
        return listGenres(keyword, model);
    }

    // Hiển thị form thêm thể loại mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        System.out.println("=== SHOWING ADD FORM ===");

        // Thêm dữ liệu cần thiết cho form
        model.addAttribute("genre", new GenreDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Add New Genre");
        model.addAttribute("activeLink", "genres");

        // Lấy danh sách tất cả movies để chọn
        try {
            List<MovieDTO> allMovies = movieService.getAllActiveMovies();
            model.addAttribute("allMovies", allMovies);
            System.out.println("Loaded " + allMovies.size() + " movies for selection");
        } catch (Exception e) {
            System.err.println("Error getting movies for add form: " + e.getMessage());
            model.addAttribute("allMovies", List.of());
        }

        // Thêm empty list cho currentMovieIds
        model.addAttribute("currentMovieIds", List.of());

        System.out.println("Returning: /admin/edit-genre");
        return "/admin/edit-genre";
    }

    // Xử lý thêm thể loại mới
    @PostMapping("/add")
    public String addGenre(@ModelAttribute("genre") GenreDTO genreDTO,
                           @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== ADDING NEW GENRE ===");
            System.out.println("Genre name: " + genreDTO.getGenreName());
            System.out.println("Selected movies: " + selectedMovies);

            // Validation đơn giản
            if (genreDTO.getGenreName() == null || genreDTO.getGenreName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên thể loại không được để trống!");
                return "redirect:/admin/genres/add";
            }

            // Kiểm tra tên thể loại có trùng không
            if (genreService.isGenreNameExists(genreDTO.getGenreName(), null)) {
                redirectAttributes.addFlashAttribute("error", "Tên thể loại đã tồn tại!");
                return "redirect:/admin/genres/add";
            }

            // Lưu thể loại
            GenreDTO savedGenre = genreService.saveGenre(genreDTO);

            // Cập nhật liên kết với movies nếu có
            if (selectedMovies != null && !selectedMovies.trim().isEmpty()) {
                genreService.updateGenreMovieAssociations(savedGenre.getGenreID(), selectedMovies);
            }

            redirectAttributes.addFlashAttribute("success", "Thêm thể loại thành công!");
        } catch (Exception e) {
            System.err.println("Error adding genre: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm thể loại: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    // Hiển thị form chỉnh sửa thể loại
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== SHOWING EDIT FORM ===");
            System.out.println("Genre ID: " + id);

            GenreDTO genre = genreService.getGenreById(id);
            if (genre == null) {
                System.out.println("ERROR: Genre not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thể loại!");
                return "redirect:/admin/genres";
            }

            System.out.println("Found genre: " + genre.getGenreName());
            model.addAttribute("genre", genre);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Edit Genre");

            // Lấy danh sách tất cả movies để chọn
            List<MovieDTO> allMovies = movieService.getAllActiveMovies();
            model.addAttribute("allMovies", allMovies);
            System.out.println("Loaded " + allMovies.size() + " movies for selection");

            // Lấy danh sách movie IDs hiện tại của genre này
            List<MovieDTO> currentMovies = movieService.getMoviesByGenre(id);
            List<Integer> currentMovieIds = currentMovies.stream()
                    .map(MovieDTO::getMovieID)
                    .collect(Collectors.toList());

            model.addAttribute("currentMovieIds", currentMovieIds);
            System.out.println("Current movie IDs for genre " + id + ": " + currentMovieIds);

            System.out.println("Returning: /admin/edit-genre");
            return "/admin/edit-genre";
        } catch (Exception e) {
            System.err.println("ERROR in showEditForm: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin thể loại: " + e.getMessage());
            return "redirect:/admin/genres";
        }
    }

    // Xử lý cập nhật thể loại
    @PostMapping("/{id}/edit")
    public String updateGenre(@PathVariable Integer id,
                              @ModelAttribute("genre") GenreDTO genreDTO,
                              @RequestParam(value = "selectedMovies", required = false) String selectedMovies,
                              RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== UPDATING GENRE ===");
            System.out.println("Genre ID: " + id);
            System.out.println("Genre name: " + genreDTO.getGenreName());
            System.out.println("Selected movies: " + selectedMovies);

            // Validation đơn giản
            if (genreDTO.getGenreName() == null || genreDTO.getGenreName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên thể loại không được để trống!");
                return "redirect:/admin/genres/" + id + "/edit";
            }

            // Kiểm tra tên thể loại có trùng không (loại trừ chính nó)
            if (genreService.isGenreNameExists(genreDTO.getGenreName(), id)) {
                redirectAttributes.addFlashAttribute("error", "Tên thể loại đã tồn tại!");
                return "redirect:/admin/genres/" + id + "/edit";
            }

            genreDTO.setGenreID(id);
            genreService.updateGenre(genreDTO);

            // Cập nhật liên kết với movies
            genreService.updateGenreMovieAssociations(id, selectedMovies != null ? selectedMovies : "");

            redirectAttributes.addFlashAttribute("success", "Cập nhật thể loại thành công!");
        } catch (Exception e) {
            System.err.println("Error updating genre: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật thể loại: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    // Xem chi tiết thể loại với movie count và danh sách phim
    @GetMapping("/{id}")
    public String viewGenreDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== VIEWING GENRE DETAIL ===");
        System.out.println("Genre ID: " + id);
        try {
            GenreDTO genre = genreService.getGenreById(id);
            if (genre == null) {
                System.out.println("ERROR: Genre not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thể loại!");
                return "redirect:/admin/genres";
            }

            // Lấy movie count cho genre này
            Long movieCount = genreService.getMovieCountByGenreID(id);

            // Lấy danh sách movies trong genre này
            List<MovieDTO> movies = movieService.getMoviesByGenre(id);

            System.out.println("Found genre: " + genre.getGenreName() + " with " + movieCount + " movies");
            model.addAttribute("genre", genre);
            model.addAttribute("movieCount", movieCount);
            model.addAttribute("movies", movies);
            model.addAttribute("pageTitle", "Genre Detail");
            model.addAttribute("activeLink", "genres");
            System.out.println("Returning: detail-genre");
            return "/admin/detail-genre";
        } catch (Exception e) {
            System.err.println("ERROR in viewGenreDetail: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải chi tiết thể loại: " + e.getMessage());
            return "redirect:/admin/genres";
        }
    }

    // Trang hiển thị danh sách phim trong thể loại
    @GetMapping("/{id}/movies")
    public String viewGenreMovies(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== VIEWING GENRE MOVIES ===");
        System.out.println("Genre ID: " + id);
        try {
            GenreDTO genre = genreService.getGenreById(id);
            if (genre == null) {
                System.out.println("ERROR: Genre not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thể loại!");
                return "redirect:/admin/genres";
            }

            // Lấy danh sách movies trong genre này
            List<MovieDTO> movies = movieService.getMoviesByGenre(id);
            Long movieCount = genreService.getMovieCountByGenreID(id);

            System.out.println("Found " + movies.size() + " movies for genre: " + genre.getGenreName());
            model.addAttribute("genre", genre);
            model.addAttribute("movies", movies);
            model.addAttribute("movieCount", movieCount);
            model.addAttribute("activeLink", "genres");

            model.addAttribute("pageTitle", "Movies in " + genre.getGenreName());

            return "/admin/genre-movies";
        } catch (Exception e) {
            System.err.println("ERROR in viewGenreMovies: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải danh sách phim: " + e.getMessage());
            return "redirect:/admin/genres";
        }
    }

    // Xóa thể loại
    @PostMapping("/{id}/delete")
    public String deleteGenre(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DELETING GENRE ===");
            System.out.println("Genre ID: " + id);

            // Kiểm tra xem genre có movies không trước khi xóa
            Long movieCount = genreService.getMovieCountByGenreID(id);
            if (movieCount > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể xóa thể loại này vì đang có " + movieCount + " phim sử dụng!");
                return "redirect:/admin/genres";
            }

            boolean deleted = genreService.deleteGenre(id);
            if (deleted) {
                System.out.println("Genre deleted successfully");
                redirectAttributes.addFlashAttribute("success", "Xóa thể loại thành công!");
            } else {
                System.out.println("Failed to delete genre");
                redirectAttributes.addFlashAttribute("error", "Không thể xóa thể loại!");
            }
        } catch (Exception e) {
            System.err.println("Error deleting genre: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa thể loại: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    // Trang kiểm tra tên thể loại (thay thế cho API check-name)
    @GetMapping("/check-name")
    public String checkGenreName(@RequestParam String name,
                                 @RequestParam(required = false) Integer excludeId,
                                 Model model) {
        boolean exists = genreService.isGenreNameExists(name, excludeId);
        model.addAttribute("nameExists", exists);
        model.addAttribute("genreName", name);
        model.addAttribute("activeLink", "genres");

        return "/admin/check-genre-name"; // Trả về view hiển thị kết quả
    }

    // ================= ADDITIONAL HELPER METHODS =================

    // Method để lấy tất cả genres cho dropdown/selection trong các form khác
    @ModelAttribute("allGenres")
    public List<GenreDTO> getAllGenresForForms() {
        try {
            return genreService.getAllGenres();
        } catch (Exception e) {
            System.err.println("Error getting all genres for forms: " + e.getMessage());
            return List.of();
        }
    }
}