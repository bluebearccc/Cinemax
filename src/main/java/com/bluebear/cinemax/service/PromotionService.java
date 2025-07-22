package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository voucherRepository;

    // ==================== VALIDATION METHODS ====================

    public boolean isValidForCreate(PromotionDTO dto) {
        return isValidCode(dto.getPromotionCode()) && isValidDiscount(dto.getDiscount()) &&
                isValidDateRange(dto.getStartTime(), dto.getEndTime()) && isValidQuantity(dto.getQuantity()) &&
                isValidStatus(dto.getStatus());
    }

    public boolean isValidForUpdate(PromotionDTO dto) {
        return (dto.getPromotionCode() == null || isValidCode(dto.getPromotionCode())) &&
                (dto.getDiscount() == null || isValidDiscount(dto.getDiscount())) &&
                (dto.getStartTime() == null || dto.getEndTime() == null || dto.getStartTime().isBefore(dto.getEndTime())) &&
                (dto.getQuantity() == null || isValidQuantity(dto.getQuantity())) &&
                (dto.getStatus() == null || isValidStatus(dto.getStatus()));
    }

    private boolean isValidCode(String code) {
        return code != null && !code.trim().isEmpty();
    }

    private boolean isValidDiscount(Integer discount) {
        return discount != null && discount >= 0 && discount <= 100;
    }

    private boolean isValidDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return start != null && end != null && start.isBefore(end);
    }

    private boolean isValidQuantity(Integer quantity) {
        return quantity != null && quantity >= 0;
    }

    private boolean isValidStatus(String status) {
        return status != null && !status.trim().isEmpty() &&
                ("Available".equals(status) || "Expired".equals(status));
    }

    // ==================== BUSINESS METHODS ====================

    public List<Promotion> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Promotion> getVoucherById(Integer id) {
        return voucherRepository.findById(id);
    }

    public Promotion createVoucher(PromotionDTO dto) {
        validateForCreate(dto);
        validateUniqueCode(dto.getPromotionCode());

        Promotion voucher = Promotion.builder()
                .promotionCode(dto.getPromotionCode())
                .discount(dto.getDiscount())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .quantity(dto.getQuantity())
                .status(Promotion_Status.valueOf(dto.getStatus()))
                .build();

        return voucherRepository.save(voucher);
    }

    public Promotion updateVoucher(Integer id, PromotionDTO dto) {
        validateForUpdate(dto);
        Promotion voucher = findVoucherById(id);

        if (dto.getPromotionCode() != null && !dto.getPromotionCode().equals(voucher.getPromotionCode())) {
            validateUniqueCode(dto.getPromotionCode());
        }

        updateVoucherFields(voucher, dto);
        validateDateRangeAfterUpdate(voucher);
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Integer id) {
        findVoucherById(id);
        voucherRepository.deleteById(id);
    }

    public List<Promotion> searchVouchers(String keyword, String status) {
        Promotion_Status enumStatus = parseStatus(status);
        return voucherRepository.searchVouchers(keyword, enumStatus);
    }

    public long getTotalVouchersCount() {
        return voucherRepository.count();
    }

    public long getActiveVouchersCount() {
        return voucherRepository.countByStatus(Promotion_Status.Available);
    }

    public long getExpiredVouchersCount() {
        return voucherRepository.countByStatus(Promotion_Status.Expired);
    }

    public double getAverageDiscountForActiveVouchers() {
        Double avg = voucherRepository.getAverageDiscount(Promotion_Status.Available);
        return avg != null ? avg : 0.0;
    }

    // ==================== HELPER METHODS ====================

    private void validateForCreate(PromotionDTO dto) {
        if (!isValidForCreate(dto)) {
            throw new IllegalArgumentException("Invalid voucher data");
        }
    }

    private void validateForUpdate(PromotionDTO dto) {
        if (!isValidForUpdate(dto)) {
            throw new IllegalArgumentException("Invalid voucher data for update");
        }
    }

    private void validateUniqueCode(String code) {
        if (voucherRepository.existsByPromotionCode(code)) {
            throw new IllegalArgumentException("Promotion code already exists");
        }
    }

    private Promotion findVoucherById(Integer id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
    }

    private void updateVoucherFields(Promotion voucher, PromotionDTO dto) {
        Optional.ofNullable(dto.getPromotionCode()).ifPresent(voucher::setPromotionCode);
        Optional.ofNullable(dto.getDiscount()).ifPresent(voucher::setDiscount);
        Optional.ofNullable(dto.getStartTime()).ifPresent(voucher::setStartTime);
        Optional.ofNullable(dto.getEndTime()).ifPresent(voucher::setEndTime);
        Optional.ofNullable(dto.getQuantity()).ifPresent(voucher::setQuantity);
        Optional.ofNullable(dto.getStatus()).ifPresent(s -> voucher.setStatus(Promotion_Status.valueOf(s)));
    }

    private void validateDateRangeAfterUpdate(Promotion voucher) {
        if (voucher.getStartTime() != null && voucher.getEndTime() != null &&
                voucher.getStartTime().isAfter(voucher.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private Promotion_Status parseStatus(String status) {
        if (status == null || status.isEmpty()) return null;
        try {
            return Promotion_Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}