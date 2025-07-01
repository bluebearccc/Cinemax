package com.bluebear.cinemax.service;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    // Get all accounts
    public List<Account> getAllAccounts() {
        try {
            logger.info("Getting all accounts from database");
            List<Account> accounts = accountRepository.findAll();
            logger.info("Found {} accounts", accounts.size());

            // Debug log first few accounts
            if (logger.isDebugEnabled() && !accounts.isEmpty()) {
                logger.debug("Sample accounts:");
                accounts.stream()
                        .limit(3)
                        .forEach(account -> logger.debug("  - ID: {}, Email: {}",
                                account.getAccountId(), account.getEmail()));
            }

            return accounts;
        } catch (Exception e) {
            logger.error("Error getting all accounts", e);
            throw new RuntimeException("Failed to retrieve accounts", e);
        }
    }

    // Get account by ID
    public Optional<Account> getAccountById(String accountId) {
        try {
            logger.debug("Getting account by ID: {}", accountId);
            Optional<Account> account = accountRepository.findById(accountId);

            if (account.isPresent()) {
                logger.debug("Account found: {}", account.get().getEmail());
            } else {
                logger.warn("Account not found with ID: {}", accountId);
            }

            return account;
        } catch (Exception e) {
            logger.error("Error getting account by ID: {}", accountId, e);
            throw new RuntimeException("Failed to retrieve account", e);
        }
    }

    // Get account by email
    public Optional<Account> getAccountByEmail(String email) {
        try {
            logger.debug("Getting account by email: {}", email);
            Optional<Account> account = accountRepository.findByEmail(email);

            if (account.isPresent()) {
                logger.debug("Account found with email: {}", email);
            } else {
                logger.debug("Account not found with email: {}", email);
            }

            return account;
        } catch (Exception e) {
            logger.error("Error getting account by email: {}", email, e);
            throw new RuntimeException("Failed to retrieve account by email", e);
        }
    }

    // Check if account ID exists
    public boolean accountIdExists(String accountId) {
        try {
            boolean exists = accountRepository.existsById(accountId);
            logger.debug("Account ID {} exists: {}", accountId, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking if account ID exists: {}", accountId, e);
            throw new RuntimeException("Failed to check account ID existence", e);
        }
    }

    // Search accounts with criteria
    public List<Account> searchAccounts(String keyword, String role, String status) {
        try {
            logger.info("Searching accounts with keyword: '{}', role: '{}', status: '{}'",
                    keyword, role, status);

            List<Account> accounts = accountRepository.findAccountsByCriteria(keyword, role, status);
            logger.info("Search found {} accounts", accounts.size());

            return accounts;
        } catch (Exception e) {
            logger.error("Error searching accounts", e);
            throw new RuntimeException("Failed to search accounts", e);
        }
    }

    // Get accounts by role
    public List<Account> getAccountsByRole(String role) {
        try {
            logger.debug("Getting accounts by role: {}", role);
            List<Account> accounts = accountRepository.findByRole(role);
            logger.debug("Found {} accounts with role: {}", accounts.size(), role);
            return accounts;
        } catch (Exception e) {
            logger.error("Error getting accounts by role: {}", role, e);
            throw new RuntimeException("Failed to retrieve accounts by role", e);
        }
    }

    // Get accounts by status
    public List<Account> getAccountsByStatus(String status) {
        try {
            logger.debug("Getting accounts by status: {}", status);
            List<Account> accounts = accountRepository.findByStatus(status);
            logger.debug("Found {} accounts with status: {}", accounts.size(), status);
            return accounts;
        } catch (Exception e) {
            logger.error("Error getting accounts by status: {}", status, e);
            throw new RuntimeException("Failed to retrieve accounts by status", e);
        }
    }

    // Save account
    public Account saveAccount(Account account) {
        try {
            logger.info("Saving new account with email: {}", account.getEmail());

            // Validate account data
            validateAccount(account);

            Account savedAccount = accountRepository.save(account);
            logger.info("Successfully saved account: {}", savedAccount.getEmail());
            return savedAccount;
        } catch (Exception e) {
            logger.error("Error saving account: {}", account.getEmail(), e);
            throw new RuntimeException("Failed to save account", e);
        }
    }

    // Update account
    public Account updateAccount(Account account) {
        try {
            logger.info("Updating account with ID: {}", account.getAccountId());

            // Validate account data
            validateAccount(account);

            Account updatedAccount = accountRepository.save(account);
            logger.info("Successfully updated account: {}", updatedAccount.getEmail());
            return updatedAccount;
        } catch (Exception e) {
            logger.error("Error updating account: {}", account.getAccountId(), e);
            throw new RuntimeException("Failed to update account", e);
        }
    }

    // Delete account
    public void deleteAccount(String accountId) {
        try {
            logger.info("Deleting account with ID: {}", accountId);

            Optional<Account> account = accountRepository.findById(accountId);
            if (account.isPresent()) {
                accountRepository.deleteById(accountId);
                logger.info("Successfully deleted account: {}", account.get().getEmail());
            } else {
                logger.warn("Attempted to delete non-existent account: {}", accountId);
                throw new RuntimeException("Account not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting account: {}", accountId, e);
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    // Check if email exists
    public boolean emailExists(String email) {
        try {
            boolean exists = accountRepository.existsByEmail(email);
            logger.debug("Email {} exists: {}", email, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking if email exists: {}", email, e);
            throw new RuntimeException("Failed to check email existence", e);
        }
    }

    // Ban account
    public void banAccount(String accountId) {
        try {
            logger.info("Banning account with ID: {}", accountId);

            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                account.setStatus("Banned");
                accountRepository.save(account);
                logger.info("Successfully banned account: {}", account.getEmail());
            } else {
                logger.warn("Attempted to ban non-existent account: {}", accountId);
                throw new RuntimeException("Account not found");
            }
        } catch (Exception e) {
            logger.error("Error banning account: {}", accountId, e);
            throw new RuntimeException("Failed to ban account", e);
        }
    }

    // Unban account
    public void unbanAccount(String accountId) {
        try {
            logger.info("Unbanning account with ID: {}", accountId);

            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                account.setStatus("Active");
                accountRepository.save(account);
                logger.info("Successfully unbanned account: {}", account.getEmail());
            } else {
                logger.warn("Attempted to unban non-existent account: {}", accountId);
                throw new RuntimeException("Account not found");
            }
        } catch (Exception e) {
            logger.error("Error unbanning account: {}", accountId, e);
            throw new RuntimeException("Failed to unban account", e);
        }
    }

    // Get account statistics
    public Map<String, Object> getAccountStatistics() {
        try {
            logger.debug("Calculating account statistics");
            Map<String, Object> stats = new HashMap<>();

            long totalAccounts = accountRepository.count();
            long activeAccounts = accountRepository.countActiveAccounts();
            long bannedAccounts = accountRepository.countBannedAccounts();
            long customers = accountRepository.countCustomers();
            long admins = accountRepository.countAdmins();

            stats.put("totalAccounts", totalAccounts);
            stats.put("activeAccounts", activeAccounts);
            stats.put("bannedAccounts", bannedAccounts);
            stats.put("totalCustomers", customers);
            stats.put("totalAdmins", admins);

            // Calculate percentages
            if (totalAccounts > 0) {
                stats.put("activePercentage", Math.round((activeAccounts * 100.0) / totalAccounts));
                stats.put("customerPercentage", Math.round((customers * 100.0) / totalAccounts));
            } else {
                stats.put("activePercentage", 0);
                stats.put("customerPercentage", 0);
            }

            logger.debug("Statistics calculated: Total={}, Active={}, Banned={}, Customers={}, Admins={}",
                    totalAccounts, activeAccounts, bannedAccounts, customers, admins);

            return stats;
        } catch (Exception e) {
            logger.error("Error calculating account statistics", e);
            throw new RuntimeException("Failed to calculate statistics", e);
        }
    }

    // Get unique roles for filter dropdown
    public List<String> getAllRoles() {
        try {
            List<String> roles = Arrays.asList("Admin", "Customer", "Staff", "Cashier", "Customer_Officer");
            logger.debug("Available roles: {}", roles);
            return roles;
        } catch (Exception e) {
            logger.error("Error getting all roles", e);
            return Arrays.asList("Admin", "Customer", "Staff"); // Fallback
        }
    }

    // Get unique statuses for filter dropdown
    public List<String> getAllStatuses() {
        try {
            List<String> statuses = Arrays.asList("Active", "Banned", "Pending");
            logger.debug("Available statuses: {}", statuses);
            return statuses;
        } catch (Exception e) {
            logger.error("Error getting all statuses", e);
            return Arrays.asList("Active", "Banned"); // Fallback
        }
    }

    // Validate account data
    private void validateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        if (account.getAccountId() == null || account.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID is required");
        }

        if (account.getEmail() == null || account.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (account.getPassword() == null || account.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (account.getRole() == null || account.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        if (account.getStatus() == null || account.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        // Validate account ID format
        if (!account.getAccountId().matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("Account ID can only contain letters, numbers, underscore, and dash");
        }

        // Validate email format
        if (!account.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password length
        if (account.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        logger.debug("Account validation passed for: {}", account.getEmail());
    }

    // Advanced search with multiple criteria
    public List<Account> advancedSearch(Map<String, Object> criteria) {
        try {
            logger.info("Performing advanced search with criteria: {}", criteria);

            String keyword = (String) criteria.get("keyword");
            String role = (String) criteria.get("role");
            String status = (String) criteria.get("status");

            List<Account> results = searchAccounts(keyword, role, status);

            // Additional filtering if needed
            if (criteria.containsKey("sortBy")) {
                String sortBy = (String) criteria.get("sortBy");
                results = sortAccounts(results, sortBy);
            }

            logger.info("Advanced search returned {} results", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in advanced search", e);
            throw new RuntimeException("Failed to perform advanced search", e);
        }
    }

    // Sort accounts
    private List<Account> sortAccounts(List<Account> accounts, String sortBy) {
        try {
            switch (sortBy.toLowerCase()) {
                case "email":
                    accounts.sort(Comparator.comparing(Account::getEmail, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "role":
                    accounts.sort(Comparator.comparing(Account::getRole, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "status":
                    accounts.sort(Comparator.comparing(Account::getStatus, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "id":
                default:
                    accounts.sort(Comparator.comparing(Account::getAccountId));
                    break;
            }

            logger.debug("Sorted {} accounts by {}", accounts.size(), sortBy);
            return accounts;

        } catch (Exception e) {
            logger.error("Error sorting accounts by: {}", sortBy, e);
            return accounts; // Return unsorted list if sorting fails
        }
    }

    // Get accounts with pagination
    public List<Account> getAccountsWithPagination(int page, int size) {
        try {
            logger.debug("Getting accounts with pagination: page={}, size={}", page, size);

            List<Account> allAccounts = getAllAccounts();
            int start = page * size;
            int end = Math.min(start + size, allAccounts.size());

            if (start >= allAccounts.size()) {
                return new ArrayList<>();
            }

            List<Account> paginatedAccounts = allAccounts.subList(start, end);
            logger.debug("Returning {} accounts for page {}", paginatedAccounts.size(), page);

            return paginatedAccounts;

        } catch (Exception e) {
            logger.error("Error getting paginated accounts", e);
            throw new RuntimeException("Failed to get paginated accounts", e);
        }
    }

    // Bulk operations
    public void bulkUpdateStatus(List<String> accountIds, String newStatus) {
        try {
            logger.info("Bulk updating status to '{}' for {} accounts", newStatus, accountIds.size());

            for (String accountId : accountIds) {
                Optional<Account> accountOpt = accountRepository.findById(accountId);
                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    account.setStatus(newStatus);
                    accountRepository.save(account);
                    logger.debug("Updated status for account: {}", account.getEmail());
                }
            }

            logger.info("Bulk status update completed successfully");

        } catch (Exception e) {
            logger.error("Error in bulk status update", e);
            throw new RuntimeException("Failed to bulk update status", e);
        }
    }

    // Export accounts data
    public List<Map<String, Object>> exportAccountsData() {
        try {
            logger.info("Exporting accounts data");

            List<Account> accounts = getAllAccounts();
            List<Map<String, Object>> exportData = new ArrayList<>();

            for (Account account : accounts) {
                Map<String, Object> accountData = new HashMap<>();
                accountData.put("accountId", account.getAccountId());
                accountData.put("email", account.getEmail());
                accountData.put("role", account.getRole());
                accountData.put("status", account.getStatus());
                exportData.add(accountData);
            }

            logger.info("Exported data for {} accounts", exportData.size());
            return exportData;

        } catch (Exception e) {
            logger.error("Error exporting accounts data", e);
            throw new RuntimeException("Failed to export accounts data", e);
        }
    }

    // Health check method
    public boolean isServiceHealthy() {
        try {
            long count = accountRepository.count();
            logger.debug("Service health check: {} accounts in database", count);
            return true;
        } catch (Exception e) {
            logger.error("Service health check failed", e);
            return false;
        }
    }
}