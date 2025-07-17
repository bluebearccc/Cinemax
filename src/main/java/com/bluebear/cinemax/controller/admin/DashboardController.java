package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.Movie.DashboardDTO;
import com.bluebear.cinemax.dto.Movie.MovieRevenueDTO;
import com.bluebear.cinemax.dto.FeedbackServiceDTO;
import com.bluebear.cinemax.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller("/admin")

public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String filter,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "1") int minRate,
                            @RequestParam(defaultValue = "5") int maxRate,
                            @RequestParam(defaultValue = "0") int feedbackPage,
                            Model model) {

        String statusFilter = null;
        if ("showing".equalsIgnoreCase(filter)) statusFilter = "SHOWING";
        else if ("coming".equalsIgnoreCase(filter)) statusFilter = "COMING_SOON";

        DashboardDTO dashboard = dashboardService.toDTO();

        Page<MovieRevenueDTO> moviePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Gọi hàm có tìm kiếm (không trừ đồ ăn)
            moviePage = dashboardService.getMovieStatsWithKeyword(statusFilter, keyword.trim().toLowerCase(), page);
        } else {
            // Gọi hàm không tìm kiếm (tính doanh thu chuẩn)
            moviePage = dashboardService.getMovieStats(statusFilter, page);
        }
        Page<FeedbackServiceDTO> feedbackPageData = dashboardService.getFeedbacksByServiceRate(minRate, maxRate, feedbackPage);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("filter", filter);
        model.addAttribute("keyword", keyword);
        // ➤ Gửi dữ liệu feedback xuống view
        model.addAttribute("feedbacks", feedbackPageData.getContent());
        model.addAttribute("feedbackCurrentPage", feedbackPage);
        model.addAttribute("feedbackTotalPages", feedbackPageData.getTotalPages());
        model.addAttribute("minRate", minRate);
        model.addAttribute("maxRate", maxRate);

        return "admin/dashboard";
    }
    @GetMapping("/admin/dashboard/revenue-trend")
    @ResponseBody
    public Map<String, Double> getRevenueTrendByBookingDate(@RequestParam(defaultValue = "7") int days) {
        return dashboardService.getRevenueTrendByBookingDate(days);
    }




}

