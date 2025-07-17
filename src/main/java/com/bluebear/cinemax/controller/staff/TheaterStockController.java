package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.service.detail_fd.DetaillFD_Service;
import com.bluebear.cinemax.service.employee.EmployeeService;
import com.bluebear.cinemax.service.invoice.InvoiceService;
import com.bluebear.cinemax.service.theater.TheaterService;
import com.bluebear.cinemax.service.theaterstock.TheaterStockService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.bluebear.cinemax.config.ExcelGeneratoForDetailItemSold;
@Controller
@RequestMapping("/staff/theater_stock")
public class TheaterStockController {

    @Autowired
    private TheaterService theaterServiceImpl;

    @Autowired
    private TheaterStockService theaterStockServiceImpl;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private InvoiceService invoiceServiceImpl;

    @Autowired
    private DetaillFD_Service detailFD_ServiceImpl;

    @GetMapping
    public String listTheaterStock(Model theModel,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(name = "itemName", required = false) String itemName,
                                   @RequestParam(name = "theaterId", required = false) Integer theaterId,
                                   @RequestParam(name = "sortField", defaultValue = "price") String sortField,
                                   @RequestParam(name = "sortDir", defaultValue = "asc") String sortDirection,
                                   HttpSession session) {

        Object employeeObj = session.getAttribute("employee");
        EmployeeDTO employee = (EmployeeDTO) employeeObj;
        Page<TheaterStockDTO> theaterStockPage;
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (theaterId != null && itemName != null && !itemName.trim().isEmpty()) {
            theaterStockPage = theaterStockServiceImpl.findByTheaterIdAndItemName(theaterId, itemName.trim(), pageable);
        }
        else if (itemName != null && !itemName.trim().isEmpty()) {
            theaterStockPage = theaterStockServiceImpl.findByItemName(itemName.trim(), pageable);
        }
        else if (theaterId != null) {
            theaterStockPage = theaterStockServiceImpl.findByTheaterId(theaterId, pageable);
        }
        else {
            theaterStockPage = theaterStockServiceImpl.getAllTheaterStock(pageable);
        }
        List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
        theModel.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        theModel.addAttribute("name", employee.getFullName());
        theModel.addAttribute("theaterStocks", theaterStockPage);
        theModel.addAttribute("currentPage", pageable.getPageNumber());
        theModel.addAttribute("allTheaters", allTheaters);
        theModel.addAttribute("totalPages", theaterStockPage.getTotalPages());
        theModel.addAttribute("selectedTheaterId", theaterId);
        theModel.addAttribute("itemName", itemName);
        theModel.addAttribute("sortField", sortField);
        theModel.addAttribute("sortDir", sortDirection);
        return "staff/list";
    }

    @GetMapping("/add_stock")
    public String showForm(Model theModel, HttpSession session) {
        boolean isAdd = true;
        List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
        EmployeeDTO e = (EmployeeDTO) session.getAttribute("employee") ;
        theModel.addAttribute("allTheaters", allTheaters);
        theModel.addAttribute("name", e.getFullName());
        theModel.addAttribute("theaterStock", new TheaterStockDTO());
        theModel.addAttribute("add", isAdd);
        return "staff/add_stock";
    }

    @PostMapping("/add_stock")
    public String processAddStock(@ModelAttribute("theaterStock") TheaterStockDTO stockInfo,
                                  @RequestParam(value = "imageInput", required = false) MultipartFile img,
                                  @RequestParam("status") TheaterStock_Status status,
                                  @RequestParam("theaterIds") List<Integer> theaterIds,
                                  RedirectAttributes redirectAttributes) {

        if (theaterIds == null || theaterIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one theater.");
            return "redirect:/staff/theater_stock/add_stock";
        }

        Optional<TheaterStockDTO> existingItemOpt = theaterStockServiceImpl.findFirstByItemName(stockInfo.getFoodName());

        if (existingItemOpt.isPresent()) {
            TheaterStockDTO existingItem = existingItemOpt.get();
            if (!stockInfo.getUnitPrice().equals(existingItem.getUnitPrice())) {
                String errorMessage = String.format(
                        "Item '%s' already exists with a price of %.2f. Please use the same price.",
                        existingItem.getFoodName(),
                        existingItem.getUnitPrice()
                );
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/staff/theater_stock/add_stock";
            }

            if (img == null || img.isEmpty()) {
                stockInfo.setImage(existingItem.getImage());
            }
        }

        for (Integer theaterId : theaterIds) {
            if (theaterStockServiceImpl.itemExistsInTheater(stockInfo.getFoodName(), theaterId)) {
                TheaterDTO existingTheater = theaterServiceImpl.getTheaterById(theaterId);
                String errorMessage = String.format("Item '%s' already exists in theater '%s'.",
                        stockInfo.getFoodName(), existingTheater.getTheaterName());
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/staff/theater_stock/add_stock";
            }
        }

        if (img != null && !img.isEmpty()) {
            try {
                String savedImagePath = theaterStockServiceImpl.saveImage(img);
                stockInfo.setImage("/uploads/theater_stocks_images/" + savedImagePath);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Could not save image file.");
                return "redirect:/staff/theater_stock/add_stock";
            }
        }

        if (stockInfo.getImage() == null || stockInfo.getImage().isEmpty()){
            redirectAttributes.addFlashAttribute("error", "Please upload an image for the new item.");
            return "redirect:/staff/theater_stock/add_stock";
        }

        stockInfo.setStatus(String.valueOf(status));

        for (Integer theaterId : theaterIds) {
            TheaterDTO theater = theaterServiceImpl.getTheaterById(theaterId);
            if (theater != null) {
                stockInfo.setTheater(theater);
                theaterStockServiceImpl.saveTheaterStock(stockInfo);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Item(s) added successfully!");
        return "redirect:/staff/theater_stock";
    }


    @GetMapping("/showFormForUpdate")
    public String showFormForUpdate(@RequestParam("id") Integer stockId, Model model) {
        TheaterStockDTO theaterStockDTO = theaterStockServiceImpl.findById(stockId);
        if (theaterStockDTO != null) {
            List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();

            List<TheaterStockDTO> siblingItems = theaterStockServiceImpl.findAllByItemName(theaterStockDTO.getFoodName());
                boolean isMultiTheaterItem = siblingItems.size() > 1;

            model.addAttribute("theaterStock", theaterStockDTO);
            model.addAttribute("allTheaters", allTheaters);
            model.addAttribute("isMultiTheaterItem", isMultiTheaterItem); // Thêm cờ báo hiệu

            return "staff/edit_stock";
        }
        return "redirect:/staff/theater_stock";
    }
    @PostMapping("/edit_stock")
    public String processEditStock(@ModelAttribute("theaterStock") TheaterStockDTO stockInfo,
                                   @RequestParam(value = "imageInput", required = false) MultipartFile newImage,
                                   RedirectAttributes redirectAttributes) {
        try {
            stockInfo.setNewImageFile(newImage);

            theaterStockServiceImpl.updateItemAcrossAllTheaters(stockInfo);

            redirectAttributes.addFlashAttribute("message", "Item updated successfully across all relevant theaters.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/theater_stock/showFormForUpdate?id=" + stockInfo.getTheaterStockId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Could not save new image.");
            return "redirect:/staff/theater_stock/showFormForUpdate?id=" + stockInfo.getTheaterStockId();
        }

        return "redirect:/staff/theater_stock";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer stockId, Model theModel,
                         RedirectAttributes redirectAttributes) {
        TheaterStockDTO theaterStockDTO = theaterStockServiceImpl.findById(stockId);
        String message = "Cannot delete because this item is sold";
        if(theaterStockServiceImpl.isDeleted(stockId)) {
            message = "Item deleted successfully!";
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/staff/theater_stock";
    }
}
