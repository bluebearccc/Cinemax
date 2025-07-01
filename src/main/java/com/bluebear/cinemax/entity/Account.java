package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @Column(name = "AccountID")
    @NotBlank(message = "Account ID is required")
    private String accountId;

    @Column(name = "Email", length = 50, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Column(name = "Password", length = 50)
    @NotBlank(message = "Password is required")
    private String password;

    @Column(name = "Role", length = 50)
    @NotBlank(message = "Role is required")
    private String role;

    @Column(name = "Status", length = 10)
    @NotBlank(message = "Status is required")
    private String status;

    // Utility methods for UI
    public String getMaskedPassword() {
        if (password == null || password.isEmpty()) return "N/A";
        return "*".repeat(Math.min(password.length(), 8));
    }

    public String getRoleBadgeClass() {
        if (role == null) return "role-badge role-unknown";

        switch (role.toLowerCase()) {
            case "admin":
                return "role-badge role-admin";
            case "customer":
                return "role-badge role-customer";
            case "staff":
                return "role-badge role-staff";
            case "cashier":
                return "role-badge role-cashier";
            case "customer_officer":
                return "role-badge role-officer";
            default:
                return "role-badge role-unknown";
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "status-badge status-unknown";

        switch (status.toLowerCase()) {
            case "active":
                return "status-badge status-active";
            case "banned":
                return "status-badge status-banned";
            case "pending":
                return "status-badge status-pending";
            default:
                return "status-badge status-unknown";
        }
    }

    // Safe email display method
    public String getDisplayEmail() {
        return (email != null && !email.trim().isEmpty()) ? email : "No Email";
    }

    // Safe role display
    public String getDisplayRole() {
        return (role != null && !role.trim().isEmpty()) ? role : "Unknown";
    }

    // Safe status display
    public String getDisplayStatus() {
        return (status != null && !status.trim().isEmpty()) ? status : "Unknown";
    }

    // Check if account is active
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    // Check if account is banned
    public boolean isBanned() {
        return "Banned".equalsIgnoreCase(status);
    }
}