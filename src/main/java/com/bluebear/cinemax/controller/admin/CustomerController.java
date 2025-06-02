package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.service.admin.CustomerService;
import com.bluebear.cinemax.service.admin.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    public String searchCustomer(@RequestParam("keyword") String keyword, Model model) {
        List<Customer> customers = customerService.findByNameContaining(keyword);
        model.addAttribute("customers", customers);
        model.addAttribute("keyword", keyword);
        return "admin/tables";
    }
    
    @GetMapping("/sort/{sort}")
    public String sortCustomer(@PathVariable("sort") String sort, Model model) {
        List<Customer> customers = null;
        if (sort.equals("asc")) {
            customers = customerService.getAllByOrderByFullNameAsc();
        } else if (sort.equals("desc")) {
            customers = customerService.getAllByOrderByFullNameDesc();
        } else {
            customers = customerService.findAll();
        }
        model.addAttribute("customers", customers);
        return "admin/tables";
    }

    @PostMapping("/add")
    public String addCustomer(@RequestParam("fullName") String fullName,
                             @RequestParam("phone") String phone,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("role") String role,
                             @RequestParam(value = "status", defaultValue = "false") boolean status,
                             RedirectAttributes redirectAttributes) {
        try {
            Account account = new Account();
            account.setEmail(email);
            account.setPassword(password); // In production, should hash this
            account.setRole(role);
            account.setStatus(status);
            
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
    
    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
        }
        return "redirect:/admin/customer";
    }
}