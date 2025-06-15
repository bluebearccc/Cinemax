package com.bluebear.cinemax.service.verifytoken;

import com.bluebear.cinemax.dto.VerifyTokenDTO;
import com.bluebear.cinemax.entity.VerifyToken;
import com.bluebear.cinemax.repository.VerifyTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerifyTokenService {

    @Autowired
    private VerifyTokenRepository verifyTokenRepository;

    // Convert Entity to DTO
    public VerifyTokenDTO toDTO(VerifyToken entity) {
        if (entity == null) return null;
        return VerifyTokenDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .password(entity.getPassword())
                .fullName(entity.getFullName())
                .build();
    }

    // Convert DTO to Entity
    public VerifyToken toEntity(VerifyTokenDTO dto) {
        if (dto == null) return null;
        return VerifyToken.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .token(dto.getToken())
                .expiresAt(dto.getExpiresAt())
                .password(dto.getPassword())
                .fullName(dto.getFullName())
                .build();
    }

    // Create new VerifyToken
    @Transactional
    public VerifyTokenDTO create(VerifyTokenDTO dto) {
        VerifyToken entity = toEntity(dto);
        entity.setId(null); // Ensure insert
        VerifyToken saved = verifyTokenRepository.save(entity);
        return toDTO(saved);
    }

    // Update existing VerifyToken
    @Transactional
    public VerifyTokenDTO update(VerifyTokenDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Id must not be null when updating");
        }

        Optional<VerifyToken> optional = verifyTokenRepository.findById(dto.getId());
        if (optional.isEmpty()) {
            throw new RuntimeException("VerifyToken with id " + dto.getId() + " not found");
        }

        VerifyToken entity = optional.get();
        entity.setEmail(dto.getEmail());
        entity.setToken(dto.getToken());
        entity.setExpiresAt(dto.getExpiresAt());
        entity.setPassword(dto.getPassword());
        entity.setFullName(dto.getFullName());

        VerifyToken updated = verifyTokenRepository.save(entity);
        return toDTO(updated);
    }

    // Delete VerifyToken by id
    @Transactional
    public void delete(Integer id) {
        if (!verifyTokenRepository.existsById(id)) {
            throw new RuntimeException("VerifyToken with id " + id + " not found");
        }
        verifyTokenRepository.deleteById(id);
    }

    // Get VerifyToken by id
    @Transactional
    public VerifyTokenDTO getById(Integer id) {
        Optional<VerifyToken> optional = verifyTokenRepository.findById(id);
        return optional.map(this::toDTO).orElse(null);
    }

    // Get token by email
    @Transactional
    public VerifyTokenDTO getTokenByEmail(String email) {
        return verifyTokenRepository.findByEmail(email)
                .map(this::toDTO)
                .orElse(null);
    }

    // Delete token by email
    @Transactional
    public void deleteTokenByEmail(String email) {
        verifyTokenRepository.deleteByEmail(email);
    }

    // Get by token string
    @Transactional
    public VerifyTokenDTO findByToken(String token) {
        return verifyTokenRepository.findByToken(token)
                .map(this::toDTO)
                .orElse(null);
    }
}
