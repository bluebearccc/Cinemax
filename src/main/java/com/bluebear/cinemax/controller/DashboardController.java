package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.service.MovieService;
import com.bluebear.cinemax.service.GenreService;
import com.bluebear.cinemax.service.ActorService;
import com.bluebear.cinemax.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private ActorService actorService;

    @Autowired(required = false) // Optional để tránh lỗi nếu VoucherService chưa được inject
    private VoucherService voucherService;

    /**
     * Trang Dashboard chính - Hiển thị thống kê tổng quan
     */
    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            // Thống kê cho dashboard
            long totalMovies = movieService.countAllMovies();
            long nowShowingMovies = movieService.countNowShowingMovies();
            long upcomingMovies = movieService.countUpcomingMovies();
            double averageRating = movieService.getAverageRating();
            long totalActors = actorService.countAllActors();
            long totalGenres = genreService.countAllGenres();

            // Thống kê voucher (nếu service có sẵn)
            long totalVouchers = 0;
            long activeVouchers = 0;
            double averageDiscount = 0.0;
            if (voucherService != null) {
                try {
                    VoucherService.VoucherStats voucherStats = voucherService.getVoucherStats();
                    totalVouchers = voucherStats.getTotalVouchers();
                    activeVouchers = voucherStats.getActiveVouchers();
                    averageDiscount = voucherStats.getAverageDiscount();
                } catch (Exception e) {
                    // Log error but continue with default values
                    e.printStackTrace();
                }
            }

            // Lấy danh sách phim mới nhất để hiển thị
            List<MovieDTO> recentMovies = movieService.getRecentMovies(5);
            List<Genre> genres = genreService.getAllGenres();

            model.addAttribute("totalMovies", totalMovies);
            model.addAttribute("nowShowingMovies", nowShowingMovies);
            model.addAttribute("upcomingMovies", upcomingMovies);
            model.addAttribute("averageRating", String.format("%.1f", averageRating));
            model.addAttribute("totalActors", totalActors);
            model.addAttribute("totalGenres", totalGenres);
            model.addAttribute("totalVouchers", totalVouchers);
            model.addAttribute("activeVouchers", activeVouchers);
            model.addAttribute("averageDiscount", String.format("%.1f%%", averageDiscount));
            model.addAttribute("recentMovies", recentMovies);
            model.addAttribute("genres", genres);
            model.addAttribute("pageTitle", "Dashboard - BlueBear Cinema");

        } catch (Exception e) {
            // Xử lý lỗi nếu service chưa có method
            model.addAttribute("totalMovies", 0L);
            model.addAttribute("nowShowingMovies", 0L);
            model.addAttribute("upcomingMovies", 0L);
            model.addAttribute("averageRating", "0.0");
            model.addAttribute("totalActors", 0L);
            model.addAttribute("totalGenres", 0L);
            model.addAttribute("totalVouchers", 0L);
            model.addAttribute("activeVouchers", 0L);
            model.addAttribute("averageDiscount", "0.0%");
            model.addAttribute("pageTitle", "Dashboard - BlueBear Cinema");
            e.printStackTrace(); // Log error for debugging
        }

        return "admin/index"; // Trả về template admin/index.html
    }

    /**
     * Trang quản lý phim
     */
    @GetMapping("/movies")
    public String adminMovies(Model model) {
        List<MovieDTO> movies = movieService.getAllMovies();
        List<Genre> genres = genreService.getAllGenres();

        // Thống kê cho movie management
        long totalMovies = movieService.countAllMovies();
        long activeMovies = movieService.countActiveMovies();
        long nowShowingMovies = movieService.countNowShowingMovies();
        double averageRating = movieService.getAverageRating();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("totalMovies", totalMovies);
        model.addAttribute("activeMovies", activeMovies);
        model.addAttribute("nowShowingMovies", nowShowingMovies);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("pageTitle", "Movie Management - Admin Panel");

        return "admin/movies";
    }

    /**
     * Trang Charts - Biểu đồ thống kê
     */
    @GetMapping("/charts")
    public String charts(Model model) {
        model.addAttribute("pageTitle", "Charts - Admin Panel");
        return "admin/charts";
    }

    /**
     * Trang Tables - Bảng dữ liệu
     */
    @GetMapping("/tables")
    public String tables(Model model) {
        model.addAttribute("pageTitle", "Tables - Admin Panel");
        return "admin/tables";
    }

    /**
     * Các trang layout demo
     */
    @GetMapping("/layout-static")
    public String layoutStatic(Model model) {
        model.addAttribute("pageTitle", "Static Navigation - Admin Panel");
        return "admin/layout-static";
    }

    @GetMapping("/layout-sidenav-light")
    public String layoutSidenavLight(Model model) {
        model.addAttribute("pageTitle", "Light Sidenav - Admin Panel");
        return "admin/layout-sidenav-light";
    }

    /**
     * Các trang authentication
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login - Admin Panel");
        return "admin/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "Register - Admin Panel");
        return "admin/register";
    }

    @GetMapping("/password")
    public String forgotPassword(Model model) {
        model.addAttribute("pageTitle", "Forgot Password - Admin Panel");
        return "admin/password";
    }

    /**
     * Các trang lỗi
     */
    @GetMapping("/401")
    public String error401(Model model) {
        model.addAttribute("pageTitle", "401 Unauthorized");
        return "admin/401";
    }

    @GetMapping("/404")
    public String error404(Model model) {
        model.addAttribute("pageTitle", "404 Not Found");
        return "admin/404";
    }

    @GetMapping("/500")
    public String error500(Model model) {
        model.addAttribute("pageTitle", "500 Internal Server Error");
        return "admin/500";
    }
}