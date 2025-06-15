package com.bluebear.cinemax.service.forgotpassword;

import com.bluebear.cinemax.dto.ForgotPasswordDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.ForgotPassword;
import com.bluebear.cinemax.repository.AccountRepository;
import com.bluebear.cinemax.repository.ForgotPasswordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;
    @Autowired
    private AccountRepository accountRepository;

    // Convert Entity to DTO
    public ForgotPasswordDTO convertToDTO(ForgotPassword entity) {
        return ForgotPasswordDTO.builder()
                .id(entity.getId())
                .otp(entity.getOtp())
                .accountId(entity.getAccount().getId())
                .expiryDate(entity.getExpiryDate())
                .build();
    }

    // Convert DTO to Entity
    public ForgotPassword convertToEntity(ForgotPasswordDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + dto.getAccountId()));

        return ForgotPassword.builder()
                .id(dto.getId())
                .otp(dto.getOtp())
                .account(account)
                .expiryDate(dto.getExpiryDate())
                .build();
    }

    // Create
    @Transactional
    public ForgotPasswordDTO createForgotPassword(ForgotPasswordDTO dto) {
        ForgotPassword entity = convertToEntity(dto);
        ForgotPassword saved = forgotPasswordRepository.save(entity);
        return convertToDTO(saved);
    }

    // Read by ID
    @Transactional
    public ForgotPasswordDTO getForgotPasswordById(Integer id) {
        ForgotPassword entity = forgotPasswordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ForgotPassword not found with ID: " + id));
        return convertToDTO(entity);
    }

    // Read all
    @Transactional
    public List<ForgotPasswordDTO> getAllForgotPasswords() {
        return forgotPasswordRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update
    @Transactional
    public ForgotPasswordDTO updateForgotPassword(Integer id, ForgotPasswordDTO dto) {
        ForgotPassword existing = forgotPasswordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ForgotPassword not found with ID: " + id));

        // Cập nhật dữ liệu
        existing.setOtp(dto.getOtp());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setAccount(accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + dto.getAccountId())));

        ForgotPassword updated = forgotPasswordRepository.save(existing);
        return convertToDTO(updated);
    }

    // Delete
    @Transactional
    public void deleteForgotPassword(Integer id) {
        if (!forgotPasswordRepository.existsById(id)) {
            throw new RuntimeException("ForgotPassword not found with ID: " + id);
        }
        forgotPasswordRepository.deleteById(id);
    }

    @Transactional
    public ForgotPasswordDTO findForgotPasswordByAccountId(Integer accountId) {
        Account account = accountRepository.findById(accountId).get();
        Optional<ForgotPassword> entity = forgotPasswordRepository.findByAccount(account);
        if (entity.isPresent()) {
            ForgotPassword forgotPassword = entity.get();
            return convertToDTO(forgotPassword);
        } else
            return null;
    }
}

