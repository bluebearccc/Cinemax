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
            redirectAttributes.addFlashAttribute("error", "Lỗi tải danh sách tài khoản!");
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

        model.addAttribute("roles", filteredRoles); // Gửi danh sách đã lọc sang view
        model.addAttribute("statuses", Account_Status.values());
        return "admin/account-add";
    }

    @PostMapping("/process-adding")
    public String saveNewAccount(@Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (accountDTO.getPassword() == null || accountDTO.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.accountDTO", "Mật khẩu không được để trống.");
        }

        if (accountDTO.getEmail() != null && !accountDTO.getEmail().trim().isEmpty()
                && accountService.findAccountByEmail(accountDTO.getEmail()).getEmail() != null) {
            bindingResult.rejectValue("email", "error.accountDTO", "Email đã tồn tại, vui lòng nhập email khác.");
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
            redirectAttributes.addFlashAttribute("success", "Thêm tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm thất bại! Đã có lỗi xảy ra.");
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
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
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
                redirectAttributes.addFlashAttribute("error", "Lỗi: không tìm thấy tài khoản!");
                return "redirect:/admin/account";
            }

            if (originalAccount.getRole() == Role.Customer && !originalAccount.getEmail().equals(accountDTO.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Không được phép thay đổi email của tài khoản Customer!");
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            if (originalAccount.getRole() == Role.Customer && accountDTO.getPassword() != null && !accountDTO.getPassword().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không được phép thay đổi mật khẩu của tài khoản Customer!");
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            if (originalAccount.getRole() == Role.Customer && accountDTO.getRole() != Role.Customer) {
                redirectAttributes.addFlashAttribute("error", "Không được phép thay đổi vai trò của tài khoản Customer!");
                redirectAttributes.addFlashAttribute("accountDTO", originalAccount);
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }

            AccountDTO existingEmailAccount = accountService.findAccountByEmail(accountDTO.getEmail());
            if (existingEmailAccount != null && !existingEmailAccount.getId().equals(originalAccount.getId())) {
                redirectAttributes.addFlashAttribute("error", "Email này đã được sử dụng bởi một tài khoản khác!");
                redirectAttributes.addFlashAttribute("accountDTO", accountDTO); // Gửi lại dữ liệu người dùng vừa nhập
                return "redirect:/admin/account/edit/" + accountDTO.getId();
            }


            accountService.updateAccount(accountDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tài khoản thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật tài khoản thất bại! " + e.getMessage());
            redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
            return "redirect:/admin/account/edit/" + accountDTO.getId();
        }
        return "redirect:/admin/account";
    }
}