package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("admin/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping()
    public String getAllAccount(@RequestParam(defaultValue = "") String keyWord,
                                @RequestParam(defaultValue = "All") String role,
                                @RequestParam(defaultValue = "All") String status,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(defaultValue = "1") Integer pageNo,
                                @RequestParam(defaultValue = "id") String sort,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            Page<AccountDTO> listAccount = accountService.searchAccounts(keyWord, role, status, pageNo, pageSize, sort);
            model.addAttribute("listAccount", listAccount);
            model.addAttribute("keyWord", keyWord);
            model.addAttribute("status", status);
            model.addAttribute("role", role);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("pageNo", pageNo);
            model.addAttribute("sort", sort);
            model.addAttribute("totalPages", listAccount.getTotalPages());
            model.addAttribute("totalItems", listAccount.getTotalElements());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading account list!"); // Translated
            return "redirect:/admin/account";
        }

        return "admin/account";
    }

    @GetMapping("/add")
    public String add(Model model) {
        if (!model.containsAttribute("accountDTO")) {
            model.addAttribute("accountDTO", new AccountDTO());
        }

        List<Role> filteredRoles = Arrays.stream(Role.values())
                .filter(r -> r != Role.Customer)
                .collect(Collectors.toList());

        model.addAttribute("roles", filteredRoles);
        model.addAttribute("statuses", Account_Status.values());
        return "admin/account-add";
    }

    @PostMapping("/process-adding")
    public String saveNewAccount(@Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (accountDTO.getPassword() == null || accountDTO.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.accountDTO", "Password cannot be empty."); // Translated
        }

        if (accountDTO.getEmail() != null && !accountDTO.getEmail().trim().isEmpty()
                && accountService.findAccountByEmail(accountDTO.getEmail()).getEmail() != null) {
            bindingResult.rejectValue("email", "error.accountDTO", "Email already exists, please enter a different email."); // Translated
        }

        if (bindingResult.hasErrors()) {
            List<Role> filteredRoles = Arrays.stream(Role.values())
                    .filter(r -> r != Role.Customer)
                    .collect(Collectors.toList());
            model.addAttribute("roles", filteredRoles);
            model.addAttribute("statuses", Account_Status.values());
            return "admin/account-add";
        }

        try {
            accountService.saveAccount(accountDTO);
            redirectAttributes.addFlashAttribute("success", "Account added successfully!"); // Translated
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add account! An error occurred."); // Translated
            redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
            return "redirect:/admin/account/add";
        }
        return "redirect:/admin/account";
    }

    @GetMapping("/edit/{accountId}")
    public String edit(@PathVariable Integer accountId, Model model, RedirectAttributes redirectAttributes) {
        AccountDTO accountDTO = null;

        if (model.containsAttribute("accountDTO")) {
            accountDTO = (AccountDTO) model.getAttribute("accountDTO");
        } else {
            accountDTO = accountService.findById(accountId);
            if (accountDTO == null) {
                redirectAttributes.addFlashAttribute("error", "Account not found!"); // Translated
                return "redirect:/admin/account";
            }
        }

        model.addAttribute("accountDTO", accountDTO);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", Account_Status.values());
        model.addAttribute("isCustomer", accountDTO.getRole() == Role.Customer);

        return "admin/account-edit";
    }

    @PostMapping("/process-editing")
    public String editAccount(@ModelAttribute AccountDTO accountDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            AccountDTO originalAccount = accountService.findById(accountDTO.getId());
            if (originalAccount == null) {
                redirectAttributes.addFlashAttribute("error", "Error: account not found!"); // Translated
                return "redirect:/admin/account";
            }

            if (originalAccount.getRole() == Role.Customer && !originalAccount.getEmail().equals(accountDTO.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Changing the email of a Customer account is not allowed!"); // Translated
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            if (originalAccount.getRole() == Role.Customer && accountDTO.getPassword() != null && !accountDTO.getPassword().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Changing the password of a Customer account is not allowed!"); // Translated
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            if (originalAccount.getRole() == Role.Customer && accountDTO.getRole() != Role.Customer) {
                redirectAttributes.addFlashAttribute("error", "Changing the role of a Customer account is not allowed!"); // Translated
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            AccountDTO existingEmailAccount = accountService.findAccountByEmail(accountDTO.getEmail());
            if (existingEmailAccount != null && !existingEmailAccount.getId().equals(originalAccount.getId())) {
                redirectAttributes.addFlashAttribute("error", "This email is already in use by another account!"); // Translated
                redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            accountService.updateAccount(accountDTO);
            redirectAttributes.addFlashAttribute("success", "Account updated successfully!"); // Translated

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update account! " + e.getMessage()); // Translated
            redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
            return "redirect:/admin/account/edit/" + accountDTO.getId();
        }
        return "redirect:/admin/account";
    }

    @GetMapping("/delete/{accountId}")
    public String deleteAccount(@PathVariable Integer accountId, RedirectAttributes redirectAttributes) {
        try {
            AccountDTO accountToUpdate = accountService.findById(accountId);
            if (accountToUpdate == null) {
                redirectAttributes.addFlashAttribute("error", "Account not found!"); // Translated
                return "redirect:/admin/account";
            }

            accountToUpdate.setStatus(Account_Status.Banned);
            accountService.updateAccount(accountToUpdate);

            redirectAttributes.addFlashAttribute("success", "Account status successfully changed to 'Removed'!"); // Translated

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error! Could not update account status. " + e.getMessage()); // Translated
        }
        return "redirect:/admin/account";
    }
}