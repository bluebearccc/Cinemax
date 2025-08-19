package com.bluebear.cinemax.controller.officer;

import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.CustomerServiceFeedbackDTO;
import com.bluebear.cinemax.dto.WatchedMovieDTO;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import com.bluebear.cinemax.repository.ServiceFeedbackRepository;
import com.bluebear.cinemax.service.admin.CustomerCareService;
import com.bluebear.cinemax.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/officer/customer-care")
public class CustomerCareController {
    private static final Logger log = LoggerFactory.getLogger(CustomerCareController.class);
    @Autowired
    private ServiceFeedbackRepository serviceFeedbackRepository;
    @Autowired
    private CustomerCareService customerCareService;
    @Autowired
    private UserProfileService userProfileService;
    @GetMapping("/feedbacks")
    public String viewFeedbacksAndDetails(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "id", required = false) Integer customerId,
            @ModelAttribute("smsStatus") String smsStatus,
            Model model) {

        // ⭐ XỬ LÝ GIÁ TRỊ "null" CHUỖI thành null thực sự:
        if ("null".equalsIgnoreCase(keyword)) {
            keyword = null;
        }
        if ("null".equalsIgnoreCase(priority)) {
            priority = null;
        }

        int pageSize = 10;
        List<CustomerServiceFeedbackDTO> allFeedbacks = customerCareService.getFilteredFeedbacks(keyword, priority);

        int totalItems = allFeedbacks.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<CustomerServiceFeedbackDTO> pagedFeedbacks = allFeedbacks.subList(fromIndex, toIndex);

        model.addAttribute("feedbackList", pagedFeedbacks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("priority", priority);
        model.addAttribute("currentWebPage", "customer-care");
        // sau khi lấy allFeedbacks
        model.addAttribute("totalItems", allFeedbacks.size());
        long resolved = serviceFeedbackRepository.countByStatus(FeedbackStatus.Suported);
        model.addAttribute("resolvedCount", resolved);
        long pending = serviceFeedbackRepository.countByStatus(FeedbackStatus.Not_Suported);
        model.addAttribute("pendingCount", pending);
// nếu muốn Đang xử lý khác với Chưa xử lý thì thêm logic, ví dụ processing = pending…
        model.addAttribute("processingCount", pending);

        if (customerId != null) {
            CustomerDTO customer = userProfileService.getCustomerById(customerId);
            List<WatchedMovieDTO> watchedMovies = userProfileService.getWatchedMovies(customer);
            CustomerServiceFeedbackDTO selectedTicket = allFeedbacks.stream()
                    .filter(f -> f.getCustomerId().equals(customerId))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("selectedTicket", selectedTicket);
            model.addAttribute("selectedCustomer", customer);
            model.addAttribute("watchedMovies", watchedMovies);


        }
        if (smsStatus != null && !smsStatus.isEmpty()) {
            model.addAttribute("smsStatus", smsStatus);
        }
        return "officer/customer-feedbacks";
    }
    @PostMapping("/feedbacks/resolve")
    public String resolveFeedback(
            @RequestParam("feedbackId") Integer feedbackId,
            RedirectAttributes redirectAttributes) {

        try {
            customerCareService.resolveFeedback(feedbackId);
            redirectAttributes.addFlashAttribute("smsStatus", "Đã đánh dấu phản hồi #" + feedbackId + " là đã giải quyết");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("smsStatus", "Lỗi khi đánh dấu: " + e.getMessage());
        }
        // quay lại trang feedbacks, giữ lại các param filter/page nếu cần
        return "redirect:/officer/customer-care/feedbacks";
    }
//    @PostMapping("/feedbacks/sendSms")
//    public String sendSmsToCustomer(
//            @RequestParam("customerId") Integer customerId,
//            @RequestParam("message") String message,
//            Model model
//    ) {
//        try {
//            customerCareService.notifyCustomerBySms(customerId, message);
//            model.addAttribute("smsStatus", "Gửi SMS thành công!");
//        } catch (Exception ex) {
//            model.addAttribute("smsStatus", "Lỗi khi gửi SMS: " + ex.getMessage());
//        }
//        // Quay lại trang feedbacks (có thể truyền lại keyword, priority, page nếu cần)
//        return "redirect:/customer-care/feedbacks?id=" + customerId;
//    }



}
