package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.room.RoomService;
import com.bluebear.cinemax.service.seat.SeatService;
import com.bluebear.cinemax.service.theater.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/theater")
public class StaffTheaterController {

    @Autowired
    private TheaterService theaterServiceImpl;

    @Autowired
    private RoomService roomServiceImpl;

    @Autowired
    private SeatService seatServiceImpl;

    @GetMapping
    public String theaterList(Model theModel) {
        List<TheaterDTO> theaters = theaterServiceImpl.findAllTheaters();
        theModel.addAttribute("theaters", theaters);
        return "staff/theater_list";
    }

    @GetMapping("/theater_detail")
    public String theaterDetail(@RequestParam("id") Integer theaterID, Model theModel) {
        TheaterDTO theater = theaterServiceImpl.getTheaterById(theaterID);
        List<RoomDTO> rooms = roomServiceImpl.findAllRoomsByTheaterId(theaterID);
        theater.setRooms(rooms);
        theModel.addAttribute("theater", theater);
        return "staff/theater_detail";
    }
    @GetMapping("/room_detail")
    public String roomDetail(@RequestParam("roomID") Integer roomID, Model theModel) {
        RoomDTO room = roomServiceImpl.getRoomById(roomID);
        List<SeatDTO> seats = seatServiceImpl.findAllByRoomId(roomID);
        room.setSeats(seats);
        theModel.addAttribute("room", room);
        theModel.addAttribute("theater", theaterServiceImpl.getTheaterById(room.getTheaterID()));
        return "staff/room_detail";
    }
    @GetMapping("/filter_status")
    public String theaterList(@RequestParam(name = "status", required = false) String status, Model theModel) {
        // Gọi phương thức service đã được cập nhật với tham số status
        List<TheaterDTO> theaters = theaterServiceImpl.findAllTheaters(status);
        theModel.addAttribute("theaters", theaters);
        // Thêm trạng thái hiện tại vào model để giữ giá trị trên dropdown sau khi lọc
        theModel.addAttribute("currentStatus", status);
        return "staff/theater_list";
    }
    @GetMapping("/search")
    public String search(@RequestParam(name = "theaterName") String theaterName, Model theModel) {
        // Gọi phương thức findAllTheatersByName từ service với từ khóa tìm kiếm
        List<TheaterDTO> theaters = theaterServiceImpl.findAllTheatersByName(theaterName); //

        theModel.addAttribute("theaters", theaters);

        theModel.addAttribute("currentKeyword", theaterName);

        return "staff/theater_list";
    }
    @GetMapping("/add_theater")
    public String showTheaterAddForm(Model theModel) {
        TheaterDTO theater = new TheaterDTO();
        theModel.addAttribute("theater", theater);
        return "staff/add_theater";
    }
    @PostMapping("/add_process")
    public String addProcess(@ModelAttribute("theater") TheaterDTO theaterDTO,
                             // --- THÊM required = false VÀO ĐÂY ---
                             @RequestParam(name = "image", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            // Gọi service để thực hiện validation và lưu
            theaterServiceImpl.addTheater(theaterDTO, imageFile);
            // Nếu thành công, gửi thông báo thành công và chuyển hướng
            redirectAttributes.addFlashAttribute("message", "Thêm rạp chiếu mới thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/theater";
        } catch (Exception e) {
            // Nếu có lỗi validation hoặc lỗi khác
            // Gửi thông báo lỗi và trả về form để người dùng sửa
            model.addAttribute("message", e.getMessage());
            model.addAttribute("messageType", "error");
            model.addAttribute("theater", theaterDTO); // Gửi lại thông tin đã nhập để không phải gõ lại
            return "staff/add_theater";
        }
    }
}
