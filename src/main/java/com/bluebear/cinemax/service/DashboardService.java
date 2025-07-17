package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.FeedbackServiceDTO;
import com.bluebear.cinemax.dto.Movie.DashboardDTO;
import com.bluebear.cinemax.dto.Movie.MovieRevenueDTO;
import com.bluebear.cinemax.entity.FeedbackService;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.FeedbackServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

@Service

public class DashboardService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private FeedbackServiceRepository feedbackServiceRepository;
    public Integer countCurrentlyShowingMovies() {
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.countCurrentlyShowing(now);
    }

    public double getRevenueToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return Optional.ofNullable(invoiceRepository.getTodayRevenue(startOfDay, endOfDay)).orElse(0.0);
    }

    public double getRevenueThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return getRevenueInRange(startOfMonth, LocalDateTime.now());
    }

    public double getRevenueThisYear() {
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        return getRevenueInRange(startOfYear, LocalDateTime.now());
    }

    public Double getRevenueInRange(LocalDateTime start, LocalDateTime end) {
        return Optional.ofNullable(invoiceRepository.getRevenueBetween(start, end)).orElse(0.0);
    }

    public long countTicketsToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return Optional.ofNullable(detailSeatRepository.countTicketsToday(startOfDay, endOfDay)).orElse(0L);
    }
    public Integer countTicketsThisYear() {
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return Optional.ofNullable(detailSeatRepository.countTicketsBetween(startOfYear, now))
                .orElse(0);
    }

    public Integer countTicketsInRange(LocalDateTime start, LocalDateTime end) {
        return detailSeatRepository.countTicketsBetween(start, end);
    }
    public Map<String, Double> getRevenueTrendByBookingDate(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        Map<String, Double> result = new LinkedHashMap<>();

        if (days == 365) {
            // ➤ Từ đầu năm đến hết hôm nay
            start = LocalDate.of(now.getYear(), 1, 1).atStartOfDay();

            List<Object[]> rawData = invoiceRepository.getRevenueByBookingDate(start, end);

            // ➤ Khởi tạo các tháng với 0
            for (int m = 1; m <= now.getMonthValue(); m++) {
                YearMonth ym = YearMonth.of(now.getYear(), m);
                result.put(ym.toString(), 0.0); // ví dụ: "2025-07"
            }

            // ➤ Ghi đè những tháng có dữ liệu
            for (Object[] row : rawData) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                YearMonth month = YearMonth.from(date);
                double revenue = ((BigDecimal) row[1]).doubleValue();
                result.merge(month.toString(), revenue, Double::sum);
            }

        } else {
            // ➤ Từng ngày
            start = LocalDate.now().minusDays(days - 1).atStartOfDay();
            List<Object[]> rawData = invoiceRepository.getRevenueByBookingDate(start, end);

            for (int i = 0; i < days; i++) {
                LocalDate date = start.toLocalDate().plusDays(i);
                result.put(date.toString(), 0.0);
            }

            for (Object[] row : rawData) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                double revenue = ((BigDecimal) row[1]).doubleValue();
                result.put(date.toString(), revenue);
            }
        }

        return result;
    }


    public DashboardDTO toDTO() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        return DashboardDTO.builder()
                .showingMovies(countCurrentlyShowingMovies())
                .revenueToday(getRevenueToday())
                .revenueMonth(getRevenueThisMonth())
                .revenueYear(getRevenueThisYear())
                .ticketsToday(countTicketsToday())
                .ticketsMonth(countTicketsInRange(startOfMonth, now))
                .ticketsThisYear(countTicketsInRange(startOfYear, now))
                .build();
    }
    public Page<MovieRevenueDTO> getMovieStats(String filter, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return movieRepository.getMovieStatistics(
                filter,
//                (keyword == null || keyword.isBlank()) ? null : keyword.toLowerCase().trim(),
                pageable
        );
    }
    public Page<MovieRevenueDTO> getMovieStatsWithKeyword(String filter, String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return movieRepository.getMovieStatisticsWithKeyword(filter, keyword, pageable);
    }
    public Page<FeedbackServiceDTO> getFeedbacksByServiceRate(int minRate, int maxRate, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<FeedbackService> feedbacks = feedbackServiceRepository
                .findByServiceRateBetweenOrderByServiceRateDesc(minRate, maxRate, pageable);

        return feedbacks.map(this::toDTO);
    }
    public FeedbackServiceDTO toDTO(FeedbackService feedback) {
        return FeedbackServiceDTO.builder()
                .id(feedback.getId())
                .customerId(feedback.getCustomer().getId())
                .createdDate(feedback.getCreatedDate())
                .content(feedback.getContent())
                .theaterId(feedback.getTheaterId())
                .serviceRate(feedback.getServiceRate())
                .status(feedback.getStatus())
                .build();
    }


}

