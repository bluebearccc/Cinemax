package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Detail_FD;
import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.service.staff.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.bluebear.cinemax.config.ExcelGeneratoForDetailItemSold;
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

    @GetMapping("/search")
    public String searchTheaterStock(@RequestParam("itemName") String itemName, Model model) {
        EmployeeDTO e = employeeService.getEmployeeById(2);
        List<TheaterStockDTO> searchResults = theaterStockServiceImpl.findByItemName(itemName, e.getTheaterId() );
        model.addAttribute("employee", e);
        model.addAttribute("theaterStocks", searchResults);
        return "staff/list";
    }

    @PostMapping("/showFormForUpdate")
    public String showFormForUpdate(@RequestParam("id") Integer stockId, Model model) {
        TheaterStockDTO theaterStockDTO = theaterStockServiceImpl.findById(stockId);
        if (theaterStockDTO != null) {
            EmployeeDTO e = employeeService.getEmployeeById(2);
            model.addAttribute("employee", e);
            model.addAttribute("theaterStock", theaterStockDTO);
            return "staff/add_edit";
        }
        return "redirect:/theater_stock";
    }

    @GetMapping("/add_stock")
    public String showForm(Model theModel) {
        boolean isAdd = true;
        List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
        theModel.addAttribute("allTheaters", allTheaters);
        EmployeeDTO e = employeeService.getEmployeeById(2);
        theModel.addAttribute("employee", e);
        theModel.addAttribute("theaterStock", new TheaterStockDTO());
        theModel.addAttribute("add", isAdd);
        return "staff/add_edit";
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
                                   @RequestParam(name = "theaterId", required = false) Integer theaterId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TheaterStockDTO> theaterStockPage;

        // Kịch bản 1: Tìm kiếm có tên VÀ có lọc theo rạp
        if (theaterId != null && itemName != null && !itemName.trim().isEmpty()) {
            theaterStockPage = theaterStockServiceImpl.findByTheaterIdAndItemName(theaterId, itemName.trim(), pageable);
        }
        // Kịch bản 2: Chỉ tìm kiếm theo tên (trên tất cả các rạp)
        else if (itemName != null && !itemName.trim().isEmpty()) {
            theaterStockPage = theaterStockServiceImpl.findByItemName(itemName.trim(), pageable);
        }
        // Kịch bản 3: Chỉ lọc theo rạp (không tìm kiếm tên)
        else if (theaterId != null) {
            theaterStockPage = theaterStockServiceImpl.findByTheaterId(theaterId, pageable);
        }
        // Kịch bản 4: Mặc định, không tìm kiếm, không lọc rạp -> Lấy tất cả
        else {
            theaterStockPage = theaterStockServiceImpl.getAllTheaterStock(pageable);
        }

        // Lấy danh sách tất cả các rạp để hiển thị trên dropdown
        List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters(); // Bạn cần có phương thức này trong TheaterService

        // Thêm các thuộc tính vào model để view có thể sử dụng
        theModel.addAttribute("employee", employeeService.getEmployeeById(1));
        theModel.addAttribute("theaterStocks", theaterStockPage);
        theModel.addAttribute("currentPage", pageable.getPageNumber());
        theModel.addAttribute("allTheaters", allTheaters);
        theModel.addAttribute("totalPages", theaterStockPage.getTotalPages());
        theModel.addAttribute("selectedTheaterId", theaterId);
        theModel.addAttribute("itemName", itemName);

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

    // Gọi service để lấy dữ liệu đã lọc và phân trang
    Page<TheaterStockDTO> resultPage = theaterStockServiceImpl.findByTheaterIdAndItemName(theaterID, itemName, pageable);

    // Thêm các thuộc tính vào model để view có thể sử dụng
    theModel.addAttribute("page", resultPage);
    theModel.addAttribute("selectedTheaterId", theaterID);
    theModel.addAttribute("itemName", itemName);

    // Lấy danh sách rạp để hiển thị lại dropdown
    List<TheaterDTO> allTheaters = theaterServiceImpl.findAllTheaters();
    theModel.addAttribute("allTheaters", allTheaters);

    return "staff/list";
}

}
