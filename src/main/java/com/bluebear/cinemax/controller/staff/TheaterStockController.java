package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.service.staff.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private DetailFD_ServiceImpl detailFDServiceImpl;
    @Autowired
    private InvoiceServiceImpl invoiceServiceImpl;
    @Autowired
    private DetailFD_ServiceImpl detailFD_ServiceImpl;

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
        return "redirect:/theater_stock";
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
            return "redirect:/theater_stock/showFormForUpdate?id=" + stockInfo.getTheaterStockId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Could not save new image.");
            return "redirect:/theater_stock/showFormForUpdate?id=" + stockInfo.getTheaterStockId();
        }

        return "redirect:/theater_stock";
    }

    @GetMapping("/add_stock")
    public String showForm(Model theModel) {
        EmployeeDTO e = employeeService.getEmployeeById(2);
        boolean isAdd = true;
        List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
        theModel.addAttribute("allTheaters", allTheaters);
        theModel.addAttribute("employee", e);
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
            return "redirect:/theater_stock/add_stock";
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
                return "redirect:/theater_stock/add_stock";
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
                return "redirect:/theater_stock/add_stock";
            }
        }

        if (img != null && !img.isEmpty()) {
            try {
                String savedImagePath = theaterStockServiceImpl.saveImage(img);
                stockInfo.setImage("/uploads/theater_stocks_images/" + savedImagePath);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Could not save image file.");
                return "redirect:/theater_stock/add_stock";
            }
        }

        if (stockInfo.getImage() == null || stockInfo.getImage().isEmpty()){
            redirectAttributes.addFlashAttribute("error", "Please upload an image for the new item.");
            return "redirect:/theater_stock/add_stock";
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
        return "redirect:/theater_stock";
    }



    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer stockId, Model theModel,
                         RedirectAttributes redirectAttributes) {
        TheaterStockDTO theaterStockDTO = theaterStockServiceImpl.findById(stockId);
        String message = "Cannot delete because this item is sold";
        if(theaterStockServiceImpl.isDeleted(stockId)) {
            if (theaterStockDTO.getImage() != null && !theaterStockDTO.getImage().isEmpty()) {
                try {
                    Path imagePath = Paths.get("uploads/images")
                            .resolve(theaterStockDTO.getImage().substring(theaterStockDTO.getImage().lastIndexOf("/") + 1));
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
    public String showForm(Model theModel, @ModelAttribute("theaterStock") TheaterStockDTO theaterStockDTO,
                           @RequestParam(value = "imageInput", required = false) MultipartFile img,
                           @RequestParam("status") TheaterStock_Status status,
                           @RequestParam("theaterID") Integer theaterID) {
        TheaterDTO theater = theaterServiceImpl.getTheaterById(theaterID);
        if (theater == null) {
            throw new RuntimeException("Theater not found with ID: " + theaterID);
        }
        theaterStockDTO.setTheater(theater);
        theaterStockDTO.setStatus(String.valueOf(status));
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
                theaterStockDTO.setImage("/uploads/images/" + filename);

            } catch (IOException e) {
                throw new RuntimeException("Could not store file. Please try again!", e);
            }
        }
        theaterStockServiceImpl.saveTheaterStock(theaterStockDTO);
        return "redirect:/theater_stock";
    }

    @GetMapping
    public String listTheaterStock(Model theModel,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(name = "itemName", required = false) String itemName,
                                   @RequestParam(name = "theaterId", required = false) Integer theaterId,
                                   @RequestParam(name = "sortField", defaultValue = "price") String sortField,
                                   @RequestParam(name = "sortDir", defaultValue = "asc") String sortDirection) {

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
        theModel.addAttribute("employee", employeeService.getEmployeeById(1));
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

//    @GetMapping("/item_sold_details")
//    public String listFD(Model theModel, @RequestParam("id") Integer stockId) {
//
//        EmployeeDTO e = employeeService.getEmployeeById(4);
//        List<Detail_FDDTO> detail_FDs = detailFDServiceImpl.findByTheaterStockID(stockId);
//        theModel.addAttribute("employee", e);
//        theModel.addAttribute("detail_FDs", detail_FDs);
//        theModel.addAttribute("itemName", theaterStockServiceImpl.findById(stockId).getFoodName() );
//
//        return "staff/item-sold";
//    }
//
//    @GetMapping("/getItemData")
//    @ResponseBody
//    public TheaterStockDTO getItemData(@RequestParam("id") Integer stockId) {
//        return theaterStockServiceImpl.findById(stockId);
//    }
//
//    @GetMapping("/export_to_excel")
//    public void exportIntoExcelFile(@RequestParam("id") Integer stockId, HttpServletResponse response) throws IOException {
//        response.setContentType("application/octet-stream");
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//        String currentDateTime = dateFormatter.format(new Date());
//
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=item_sold_details_" + currentDateTime + ".xlsx";
//        response.setHeader(headerKey, headerValue);
//
//        List <Detail_FDDTO> listFDs = detailFD_ServiceImpl.findByTheaterStockID(stockId);
//        ExcelGeneratoForDetailItemSold generator = new ExcelGeneratoForDetailItemSold(listFDs);
//        generator.generateExcelFile(response);
//    }
@GetMapping("/theaters_filter")
public String getTheaterFilterPage(Model theModel,
                                   @RequestParam("theaterId") Integer theaterID,
                                   @RequestParam(name = "itemName", required = false) String itemName, // Nên để itemName không bắt buộc
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);

    Page<TheaterStockDTO> resultPage = theaterStockServiceImpl.findByTheaterIdAndItemName(theaterID, itemName, pageable);

    theModel.addAttribute("page", resultPage);
    theModel.addAttribute("selectedTheaterId", theaterID);
    theModel.addAttribute("itemName", itemName);

    List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
    theModel.addAttribute("allTheaters", allTheaters);

    return "staff/list";
}

}
