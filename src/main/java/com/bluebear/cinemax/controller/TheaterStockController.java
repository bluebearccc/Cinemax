package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.service.*;
import com.bluebear.cinemax.entity.Detail_FD;
import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.entity.TheaterStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/theater_stock")
public class TheaterStockController {

    @Autowired
    private TheaterServiceImpl theaterServiceImpl;

    @Autowired
    private TheaterStockServiceImpl theaterStockServiceImpl;

    @Autowired
    private EmployeeServiceImpl employeeService;

    @Autowired
    private DetailFD_ServiceImpl detailFDService;

    @GetMapping("/search")
    public String searchTheaterStock(@RequestParam("itemName") String itemName, Model model) {
        Employee e = employeeService.getEmployeeById(2);
        List<TheaterStock> searchResults = theaterStockServiceImpl.findByItemName(itemName);
        model.addAttribute("employee", e);
        model.addAttribute("theaterStocks", searchResults);
        model.addAttribute("size", searchResults.size());
        model.addAttribute("theaterName", e.getTheater().getTheaterName());
        return "staff/tables";
    }

    @PostMapping("/showFormForUpdate")
    public String showFormForUpdate(@RequestParam("stockId") Integer stockId, Model model) {
        TheaterStock theaterStock = theaterStockServiceImpl.findById(stockId);
        if (theaterStock != null) {
            Employee e = employeeService.getEmployeeById(2);
            model.addAttribute("employee", e);
            model.addAttribute("theaterStock", theaterStock);
            return "staff/add-new-item";
        }
        return "redirect:/theater_stock";
    }

    @GetMapping("/add_stock")
    public String showForm(Model theModel) {
        Employee e = employeeService.getEmployeeById(2);
        theModel.addAttribute("employee", e);
        theModel.addAttribute("theaterStock", new TheaterStock());
        return "staff/add-new-item";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("stockId") Integer stockId, Model theModel,
                         RedirectAttributes redirectAttributes) {
        TheaterStock theaterStock = theaterStockServiceImpl.findById(stockId);
        String message = "Canot delete because item is sold";
        if(theaterStockServiceImpl.isDeleted(stockId)) {
            if (theaterStock.getImage() != null && !theaterStock.getImage().isEmpty()) {
                try {
                    Path imagePath = Paths.get("uploads/images")
                            .resolve(theaterStock.getImage().substring(theaterStock.getImage().lastIndexOf("/") + 1));
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            message = "Item deleted successfully!";
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/theater_stock";
    }

    @PostMapping("/process_stock")
    public String showForm(Model theModel, @ModelAttribute("theaterStock") TheaterStock theaterStock,
                           @RequestParam(value = "imageInput", required = false) MultipartFile img,
                           @RequestParam("theaterID") Integer theaterID) {

        if(theaterStock.getTheater() == null) {
            Theater theater = theaterServiceImpl.getTheaterById(theaterID);
            theaterStock.setTheater(theater);
        }
        if(img != null && !img.isEmpty()) {
            try {
                String uploadDir = "uploads/images";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String filename = img.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                if (Files.exists(filePath)) {
                    int counter = 1;
                    String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
                    String extension = filename.substring(filename.lastIndexOf('.'));
                    while (Files.exists(filePath)) {
                        filename = nameWithoutExtension + "(" + counter + ")" + extension;
                        filePath = uploadPath.resolve(filename);
                        counter++;
                    }
                }

                Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                theaterStock.setImage("/uploads/images/" + filename);

            } catch (IOException e) {
                throw new RuntimeException("Could not store file. Please try again!", e);
            }
        }
        theaterStockServiceImpl.saveTheaterStock(theaterStock);
        return "redirect:/theater_stock";
    }

    @GetMapping
    public String listTheaterStock(Model theModel) {
        Employee e = employeeService.getEmployeeById(2);
        theModel.addAttribute("employee", e);
        theModel.addAttribute("theaterStocks", theaterStockServiceImpl.findByTheaterId(e.getTheater().getTheaterId()));
        theModel.addAttribute("size", theaterStockServiceImpl.findByTheaterId(e.getTheater().getTheaterId()).size());
        theModel.addAttribute("theaterName", theaterStockServiceImpl.findByTheaterId(e.getTheater().getTheaterId()).get(0).getTheater().getTheaterName());
        return "staff/tables";
    }

    @GetMapping("/item_sold_details")
    public String listFD(Model theModel, @RequestParam("stockId") Integer stockId) {
        Employee e = employeeService.getEmployeeById(2);
        theModel.addAttribute("employee", e);
        List<Detail_FD> detail_FDs = detailFDService.findByTheaterStockID(stockId);
        theModel.addAttribute("detail_FDs", detail_FDs);
        return "staff/view-items-sold";
    }
}