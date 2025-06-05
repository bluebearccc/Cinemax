package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.service.admin.CustomerService;
import com.bluebear.cinemax.service.admin.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String getAllCustomer(Model model) {
        List<Customer> customers = customerService.findAll();
        model.addAttribute("customers", customers);
        return "admin/tables";
    }

    @GetMapping("/search")
    public String searchCustomer(@RequestParam("fullName") String keyword, Model model) {
        List<Customer> customers = customerService.findByNameContaining(keyword);
        model.addAttribute("customers", customers);
        model.addAttribute("keyword", keyword);
        return "admin/tables";
    }

    @GetMapping("/sort/{sort}")
    public String sortCustomer(@PathVariable("sort") String sort,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {
        List<Customer> customers = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (sort.equals("asc")) {
                customers = customerService.findByNameContainingOrderByFullNameAsc(keyword);
            } else if (sort.equals("desc")) {
                customers = customerService.findByNameContainingOrderByFullNameDesc(keyword);
            } else {
                customers = customerService.findByNameContaining(keyword);
            }
            model.addAttribute("keyword", keyword);
        } else {
            if (sort.equals("asc")) {
                customers = customerService.getAllByOrderByFullNameAsc();
            } else if (sort.equals("desc")) {
                customers = customerService.getAllByOrderByFullNameDesc();
            } else {
                customers = customerService.findAll();
            }
        }
        model.addAttribute("customers", customers);
        return "admin/tables";
    }

    @PostMapping("/add")
    public String addCustomer(@RequestParam("fullName") String fullName,
                              @RequestParam(value = "phone", required = false) String phone,
                              @RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam("role") String role,
                              @RequestParam(value = "statusChecked", required = false) String statusChecked,
                              RedirectAttributes redirectAttributes) {

        if(accountService.findByEmail(email).isPresent()){
            redirectAttributes.addFlashAttribute("emailError", "Email already exists!");
            return "redirect:/admin/customer";
        }

        if (phone == null || !phone.matches("^09\\d{8}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number must be 10 digits and start with 09");
            return "redirect:/admin/customer";
        }

        try {
            Account account = new Account();
            account.setEmail(email);
            account.setPassword(password);
            account.setRole(role);
            account.setStatus(statusChecked != null && "true".equals(statusChecked));
            Account savedAccount = accountService.save(account);

            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setAccount(savedAccount);

            customerService.save(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding customer: " + e.getMessage());
        }

        return "redirect:/admin/customer";
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.findCustomerById(id);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            if (customer.getAccount().isStatus()) {
                return ResponseEntity.badRequest().body("Cannot delete active customer. Please deactivate first.");
            }

            customerService.deleteById(id);

            return ResponseEntity.ok().body("Customer deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting customer: " + e.getMessage());
        }
    }


    @PostMapping("/{id}/{action}")
    public String updateCustomerStatus(@PathVariable("id") int id,
                                       @PathVariable("action") String action,
                                       RedirectAttributes redirectAttributes) {
        boolean active = !(action.equalsIgnoreCase("activate") || action.equalsIgnoreCase("active"));
        accountService.updateStatus(id, active);
        redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully.");
        return "redirect:/admin/customer";
    }
}