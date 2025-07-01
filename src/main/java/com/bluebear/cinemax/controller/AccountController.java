package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    // Display all accounts
    @GetMapping
    public String listAccounts(Model model) {
        try {
            logger.info("=== LOADING ALL ACCOUNTS ===");

            List<Account> accounts = accountService.getAllAccounts();
            Map<String, Object> stats = accountService.getAccountStatistics();

            // Debug logging
            logger.info("Total accounts found: {}", accounts.size());

            if (logger.isDebugEnabled()) {
                accounts.forEach(account -> {
                    logger.debug("Account: ID='{}', Email='{}', Role='{}', Status='{}'",
                            account.getAccountId(),
                            account.getEmail(),
                            account.getRole(),
                            account.getStatus());
                });
            }

            // Add attributes to model
            model.addAttribute("accounts", accounts);
            model.addAttribute("stats", stats);
            model.addAttribute("pageTitle", "Account Management");
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());

            // Debug attributes
            model.addAttribute("debugMode", true);
            model.addAttribute("totalAccountsDebug", accounts.size());

            logger.info("Successfully loaded {} accounts", accounts.size());
            return "admin/list-account";

        } catch (Exception e) {
            logger.error("Error loading accounts", e);
            model.addAttribute("error", "Error loading accounts: " + e.getMessage());
            model.addAttribute("accounts", List.of());
            model.addAttribute("stats", Map.of());
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());
            return "admin/list-account";
        }
    }

    // Search accounts
    @GetMapping("/search")
    public String searchAccounts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        try {
            logger.info("=== SEARCHING ACCOUNTS ===");
            logger.info("Search params - Keyword: '{}', Role: '{}', Status: '{}'",
                    keyword, role, status);

            List<Account> accounts = accountService.searchAccounts(keyword, role, status);
            Map<String, Object> stats = accountService.getAccountStatistics();

            logger.info("Search results: {} accounts found", accounts.size());

            model.addAttribute("accounts", accounts);
            model.addAttribute("stats", stats);
            model.addAttribute("pageTitle", "Account Search Results");
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedRole", role);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());

            return "admin/list-account";

        } catch (Exception e) {
            logger.error("Error searching accounts", e);
            model.addAttribute("error", "Error searching accounts: " + e.getMessage());
            model.addAttribute("accounts", List.of());
            model.addAttribute("stats", Map.of());
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());
            return "admin/list-account";
        }
    }

    // View account details
    @GetMapping("/{id}")
    public String viewAccount(@PathVariable("id") String accountId, Model model) {
        try {
            logger.info("Viewing account details for ID: {}", accountId);

            Optional<Account> accountOpt = accountService.getAccountById(accountId);

            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                logger.info("Account found: {}", account.getEmail());

                model.addAttribute("account", account);
                model.addAttribute("pageTitle", "Account Details - " + account.getEmail());
                return "admin/detail-account";
            } else {
                logger.warn("Account not found with ID: {}", accountId);
                model.addAttribute("error", "Account not found with ID: " + accountId);
                return "redirect:/admin/accounts";
            }

        } catch (Exception e) {
            logger.error("Error loading account details for ID: {}", accountId, e);
            model.addAttribute("error", "Error loading account details: " + e.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    // Show add account form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        logger.info("Showing add account form");

        model.addAttribute("account", new Account());
        model.addAttribute("pageTitle", "Add New Account");
        model.addAttribute("roles", accountService.getAllRoles());
        model.addAttribute("statuses", accountService.getAllStatuses());
        model.addAttribute("isEdit", false);
        return "admin/form-account";
    }

    // Save new account
    @PostMapping("/add")
    public String addAccount(@Valid @ModelAttribute Account account,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            logger.info("Adding new account with email: {}", account.getEmail());

            // Check validation errors
            if (bindingResult.hasErrors()) {
                logger.warn("Validation errors for new account: {}", bindingResult.getAllErrors());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Please fix the validation errors and try again.");
                return "admin/form-account";
            }

            // Check if account ID already exists
            if (accountService.getAccountById(account.getAccountId()).isPresent()) {
                logger.warn("Account ID already exists: {}", account.getAccountId());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Account ID already exists: " + account.getAccountId());
                return "admin/form-account";
            }

            // Check if email already exists
            if (accountService.emailExists(account.getEmail())) {
                logger.warn("Email already exists: {}", account.getEmail());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", false);
                model.addAttribute("error", "Email already exists: " + account.getEmail());
                return "admin/form-account";
            }

            Account savedAccount = accountService.saveAccount(account);
            logger.info("Successfully created account: {}", savedAccount.getEmail());

            redirectAttributes.addFlashAttribute("success",
                    "Account created successfully: " + savedAccount.getEmail());
            return "redirect:/admin/accounts";

        } catch (Exception e) {
            logger.error("Error creating account: {}", account.getEmail(), e);
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Error creating account: " + e.getMessage());
            return "admin/form-account";
        }
    }

    // Show edit form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") String accountId, Model model) {
        try {
            logger.info("Showing edit form for account ID: {}", accountId);

            Optional<Account> accountOpt = accountService.getAccountById(accountId);

            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                logger.info("Editing account: {}", account.getEmail());

                model.addAttribute("account", account);
                model.addAttribute("pageTitle", "Edit Account - " + account.getEmail());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", true);
                return "admin/form-account";
            } else {
                logger.warn("Account not found for edit with ID: {}", accountId);
                model.addAttribute("error", "Account not found with ID: " + accountId);
                return "redirect:/admin/accounts";
            }

        } catch (Exception e) {
            logger.error("Error loading account for edit: {}", accountId, e);
            model.addAttribute("error", "Error loading account for edit: " + e.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    // Update account
    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable("id") String accountId,
                                @Valid @ModelAttribute Account account,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            logger.info("Updating account ID: {} with email: {}", accountId, account.getEmail());

            // Check validation errors
            if (bindingResult.hasErrors()) {
                logger.warn("Validation errors for account update: {}", bindingResult.getAllErrors());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Please fix the validation errors and try again.");
                return "admin/form-account";
            }

            // Check if email exists for another account
            Optional<Account> existingAccountWithEmail = accountService.getAccountByEmail(account.getEmail());
            if (existingAccountWithEmail.isPresent() &&
                    !existingAccountWithEmail.get().getAccountId().equals(accountId)) {
                logger.warn("Email already exists for another account: {}", account.getEmail());
                model.addAttribute("roles", accountService.getAllRoles());
                model.addAttribute("statuses", accountService.getAllStatuses());
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Email already exists for another account: " + account.getEmail());
                return "admin/form-account";
            }

            account.setAccountId(accountId);
            Account updatedAccount = accountService.updateAccount(account);
            logger.info("Successfully updated account: {}", updatedAccount.getEmail());

            redirectAttributes.addFlashAttribute("success",
                    "Account updated successfully: " + updatedAccount.getEmail());
            return "redirect:/admin/accounts";

        } catch (Exception e) {
            logger.error("Error updating account: {}", accountId, e);
            model.addAttribute("roles", accountService.getAllRoles());
            model.addAttribute("statuses", accountService.getAllStatuses());
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Error updating account: " + e.getMessage());
            return "admin/form-account";
        }
    }

    // Ban account
    @PostMapping("/{id}/ban")
    public String banAccount(@PathVariable("id") String accountId,
                             RedirectAttributes redirectAttributes) {
        try {
            logger.info("Banning account ID: {}", accountId);

            accountService.banAccount(accountId);
            logger.info("Successfully banned account ID: {}", accountId);

            redirectAttributes.addFlashAttribute("success", "Account banned successfully");
            return "redirect:/admin/accounts";

        } catch (Exception e) {
            logger.error("Error banning account: {}", accountId, e);
            redirectAttributes.addFlashAttribute("error",
                    "Error banning account: " + e.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    // Unban account
    @PostMapping("/{id}/unban")
    public String unbanAccount(@PathVariable("id") String accountId,
                               RedirectAttributes redirectAttributes) {
        try {
            logger.info("Unbanning account ID: {}", accountId);

            accountService.unbanAccount(accountId);
            logger.info("Successfully unbanned account ID: {}", accountId);

            redirectAttributes.addFlashAttribute("success", "Account unbanned successfully");
            return "redirect:/admin/accounts";

        } catch (Exception e) {
            logger.error("Error unbanning account: {}", accountId, e);
            redirectAttributes.addFlashAttribute("error",
                    "Error unbanning account: " + e.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    // Delete account
    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable("id") String accountId,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting account ID: {}", accountId);

            // Get account info before deletion for logging
            Optional<Account> accountOpt = accountService.getAccountById(accountId);
            String accountEmail = accountOpt.map(Account::getEmail).orElse("Unknown");

            accountService.deleteAccount(accountId);
            logger.info("Successfully deleted account ID: {} ({})", accountId, accountEmail);

            redirectAttributes.addFlashAttribute("success",
                    "Account deleted successfully: " + accountEmail);
            return "redirect:/admin/accounts";

        } catch (Exception e) {
            logger.error("Error deleting account: {}", accountId, e);
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting account: " + e.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    // Additional utility endpoints

    // Check if account ID is available (AJAX endpoint)
    @GetMapping("/check-id/{id}")
    @ResponseBody
    public Map<String, Object> checkAccountIdAvailability(@PathVariable("id") String accountId) {
        try {
            boolean exists = accountService.getAccountById(accountId).isPresent();
            Map<String, Object> response = Map.of(
                    "available", !exists,
                    "message", exists ? "Account ID already exists" : "Account ID is available"
            );
            return response;
        } catch (Exception e) {
            return Map.of(
                    "available", false,
                    "message", "Error checking account ID"
            );
        }
    }

    // Check if email is available (AJAX endpoint)
    @GetMapping("/check-email")
    @ResponseBody
    public Map<String, Object> checkEmailAvailability(@RequestParam String email,
                                                      @RequestParam(required = false) String excludeId) {
        try {
            Optional<Account> existingAccount = accountService.getAccountByEmail(email);
            boolean isAvailable = existingAccount.isEmpty() ||
                    (excludeId != null && existingAccount.get().getAccountId().equals(excludeId));

            Map<String, Object> response = Map.of(
                    "available", isAvailable,
                    "message", isAvailable ? "Email is available" : "Email already exists"
            );
            return response;
        } catch (Exception e) {
            return Map.of(
                    "available", false,
                    "message", "Error checking email"
            );
        }
    }
}