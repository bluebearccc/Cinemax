package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.SeatUpdateRequest;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.service.staff.RoomServiceImpl;
import com.bluebear.cinemax.service.staff.SeatService;
import com.bluebear.cinemax.service.staff.TheaterService;
import com.bluebear.cinemax.service.staff.TheaterServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/theater")
public class TheaterController {

    @Autowired
    private TheaterServiceImpl theaterServiceImpl;

    @Autowired
    private RoomServiceImpl roomServiceImpl;

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

    @PostMapping("/add_theater")
    public String addTheater(
            @ModelAttribute("theater") TheaterDTO theaterDTO,
            @RequestParam("imageInput") MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            theaterServiceImpl.addTheater(theaterDTO, imageFile);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Theater added successfully!");
            return "redirect:/theater";

        } catch (Exception e) {
            model.addAttribute("messageType", "error");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("theater", theaterDTO);
            return "staff/add_theater";
        }
    }

    @GetMapping("/showFormForUpdate")
    public String showUpdate(
            @RequestParam("id") Integer theaterId,
            Model model){
        TheaterDTO theaterDTO = theaterServiceImpl.getTheaterById(theaterId);
        model.addAttribute("theater", theaterDTO);
        return "staff/edit_theater";
    }

    @PostMapping("/edit_theater")
    public String processEditTheater(
            @ModelAttribute("theater") TheaterDTO theaterDTO,
            Model model,
            @RequestParam("imageInput") MultipartFile imageFile,
            RedirectAttributes redirectAttributes){
        try {
            theaterDTO.setNewImage(imageFile);
            theaterServiceImpl.updateTheater(theaterDTO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/showFormForUpdate?id="+theaterDTO.getTheaterID();
        }
        return "redirect:/theater";
    }

    @GetMapping("/add_room")
    public String showRoomAddForm(Model theModel, @RequestParam("theaterID") Integer theaterID) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setTheaterID(theaterID);
        theModel.addAttribute("room", roomDTO);
        return "staff/add_room";
    }
    @PostMapping("/add_room")
    public String addRoom(
            @ModelAttribute("room") RoomDTO roomDTO,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            RoomDTO savedRoom = roomServiceImpl.addRoom(roomDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Room'" + savedRoom.getName() + "' added. Please set up the seats for this room.");
            return "redirect:/theater/edit_seats_for_room?roomId=" + savedRoom.getRoomID();

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("room", roomDTO);
            return "staff/add_room";
        }
    }
    @GetMapping("/edit_seats_for_room")
    public String showEditSeatsPage(@RequestParam("roomId") Integer roomId, Model model) {
        RoomDTO room = roomServiceImpl.getRoomById(roomId);

        if (room == null || room.getSeats() == null) {
            return "redirect:/theater";
        }

        Map<Character, List<SeatDTO>> seatsByRow = room.getSeats().stream()
                .collect(Collectors.groupingBy(
                        seat -> seat.getPosition().charAt(0),
                        Collectors.toList()
                ));

        List<Character> sortedRowLabels = new ArrayList<>(seatsByRow.keySet());
        java.util.Collections.sort(sortedRowLabels);
        model.addAttribute("room", room);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("sortedRowLabels", sortedRowLabels);
        model.addAttribute("seatUpdateRequest", new SeatUpdateRequest());

        return "staff/edit_seats_for_room";
    }

    @PostMapping("/update_seats")
    public String updateSeats(
            @ModelAttribute("seatUpdateRequest") SeatUpdateRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            seatServiceImpl.updateSeatsVipAndPrice(request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ghế và giá vé thành công!");
            RoomDTO room = roomServiceImpl.getRoomById(request.getRoomId());
            return "redirect:/theater/theater_detail?id=" + room.getTheaterID();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/theater/edit_seats_for_room?roomId=" + request.getRoomId();
        }
    }
    @DeleteMapping("/seats/{seatId}")
    @ResponseBody
    public ResponseEntity<?> deleteSeat(@PathVariable("seatId") Integer seatId) {
        try {
            List<SeatDTO> updatedSeats = seatServiceImpl.deleteSeatById(seatId);
            return ResponseEntity.ok(updatedSeats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting seat: " + e.getMessage());
        }
    }
    @GetMapping("/delete_room")
    public String deleteRoom(@RequestParam("roomID") Integer roomId, RedirectAttributes redirectAttributes) {
        Integer theaterId = null;
        try {
            theaterId = roomServiceImpl.findTheaterIdByRoomId(roomId);

            roomServiceImpl.deleteRoomById(roomId);

            redirectAttributes.addFlashAttribute("message", "Room deleted successfully.");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            // Bắt lỗi từ service (ví dụ: phòng có lịch chiếu) và hiển thị cho người dùng
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        if (theaterId == null) {
            return "redirect:/theater";
        }
        return "redirect:/theater/theater_detail?id=" + theaterId;
    }
}
