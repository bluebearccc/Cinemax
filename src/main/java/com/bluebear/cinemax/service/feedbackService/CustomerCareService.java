package com.bluebear.cinemax.service.feedbackService;


import com.bluebear.cinemax.dto.CustomerServiceFeedbackDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.FeedbackService;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import com.bluebear.cinemax.repository.AccountRepository;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.FeedbackServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerCareService {

    @Autowired
    private FeedbackServiceRepository serviceFeedbackRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;


    public List<CustomerServiceFeedbackDTO> getUnresolvedServiceFeedbacks() {
        List<FeedbackService> feedbacks = serviceFeedbackRepository.findByStatus(FeedbackStatus.Not_Suported);

        return feedbacks.stream()
                .map(fb -> {
                    Customer customer = customerRepository.findById(fb.getCustomer().getId()).orElse(null);
                    if (customer == null) return null;

                    Account account = customer.getAccount();

                    return CustomerServiceFeedbackDTO.builder()
                            .feedbackId(fb.getId())
                            .fullName(customer.getFullName())
                            .customerId(customer.getId())
                            .email(account != null ? account.getEmail() : "N/A")
                            .phone(customer.getPhone())
                            .content(fb.getContent())
                            .status(fb.getStatus().toString())
                            .serviceRate(fb.getServiceRate())
                            .theaterId(fb.getTheaterId())
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    public List<CustomerServiceFeedbackDTO> searchFeedbacks(String keyword) {
        return getUnresolvedServiceFeedbacks().stream()
                .filter(fb -> {
                    String lower = keyword.toLowerCase();
                    return (fb.getFullName() != null && fb.getFullName().toLowerCase().contains(lower)) ||
                            (fb.getEmail() != null && fb.getEmail().toLowerCase().contains(lower)) ||
                            (fb.getPhone() != null && fb.getPhone().contains(lower));
                })
                .collect(Collectors.toList());
    }
    public List<CustomerServiceFeedbackDTO> getFilteredFeedbacks(String keyword, String priority) {
        List<CustomerServiceFeedbackDTO> feedbacks = getUnresolvedServiceFeedbacks();

        if (keyword != null && !keyword.isEmpty()) {
            feedbacks = feedbacks.stream().filter(fb ->
                    (fb.getFullName() != null && fb.getFullName().toLowerCase().contains(keyword.toLowerCase())) ||
                            (fb.getPhone() != null && fb.getPhone().contains(keyword)) ||
                            (fb.getEmail() != null && fb.getEmail().toLowerCase().contains(keyword.toLowerCase()))
            ).collect(Collectors.toList());
        }

        if (priority != null && !priority.isEmpty()) {
            feedbacks = feedbacks.stream().filter(fb -> {
                if ("HIGH".equalsIgnoreCase(priority)) return fb.getServiceRate() == 1;
                if ("MEDIUM".equalsIgnoreCase(priority)) return fb.getServiceRate() == 3;
                if ("LOW".equalsIgnoreCase(priority)) return fb.getServiceRate() == 2;
                return true;
            }).collect(Collectors.toList());
        }

        return feedbacks;
    }
//    public void notifyCustomerBySms(Integer customerId, String customMessage) {
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với id " + customerId));
//        String phone = customer.getPhone();
//        if (phone == null || phone.isEmpty()) {
//            throw new IllegalStateException("Khách hàng chưa có số điện thoại");
//        }
//        smsService.sendSms(phone, customMessage);
//    }
    public void resolveFeedback(Integer feedbackId) {
        FeedbackService fb = serviceFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy feedback với id " + feedbackId));
        fb.setStatus(FeedbackStatus.Suported);
        serviceFeedbackRepository.save(fb);
    }

}
