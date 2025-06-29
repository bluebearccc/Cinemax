package com.bluebear.cinemax.service.forgotpassword;

import com.bluebear.cinemax.dto.ForgotPasswordDTO;
import com.bluebear.cinemax.entity.ForgotPassword;

import java.util.List;

public interface ForgotPasswordService {
    // CRUD
    ForgotPasswordDTO createForgotPassword(ForgotPasswordDTO dto);

    ForgotPasswordDTO getForgotPasswordById(Integer id);

    List<ForgotPasswordDTO> getAllForgotPasswords();

    ForgotPasswordDTO updateForgotPassword(Integer id, ForgotPasswordDTO dto);

    void deleteForgotPassword(Integer id);

    ForgotPasswordDTO findForgotPasswordByAccountId(Integer accountId);

    // Convert methods
    ForgotPasswordDTO toDTO(ForgotPassword entity);

    ForgotPassword ToEntity(ForgotPasswordDTO dto);
}
