package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.config.MapService;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.service.staff.RoomServiceImpl;
import com.bluebear.cinemax.service.staff.SeatImpl;
import com.bluebear.cinemax.service.staff.SeatService;
import com.bluebear.cinemax.service.staff.TheaterServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/theater")
public class TheaterController {

    @Autowired
    private TheaterServiceImpl theaterServiceImpl;

    @Autowired
    private RoomServiceImpl roomServiceImpl;

    @Autowired
    private SeatImpl seatServiceImpl;

    @Autowired
    private MapService mapService;

    @GetMapping
    public String theaterList(Model model,
                              @RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "5") int size,
                              @RequestParam(name = "sortField", defaultValue = "theaterName") String sortField,
                              @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {

        // 1. Tạo đối tượng Sort và Pageable (giữ nguyên)
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Sử dụng if/else để gọi phương thức service phù hợp
        Page<TheaterDTO> theaterPage;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All");

        if (hasKeyword && hasStatus) {
            theaterPage = theaterServiceImpl.findByKeywordAndStatusPaginated(keyword, status, pageable);
        } else if (hasKeyword) {
            theaterPage = theaterServiceImpl.findByKeywordPaginated(keyword, pageable);
        } else if (hasStatus) {
            theaterPage = theaterServiceImpl.findByStatusPaginated(status, pageable);
        } else {
            theaterPage = theaterServiceImpl.findAllPaginated(pageable);
        }

        // 3. Thêm các thuộc tính vào Model để View sử dụng (giữ nguyên)
        model.addAttribute("theaters", theaterPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", theaterPage.getTotalPages());
        model.addAttribute("totalItems", theaterPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentStatus", status);

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

    @GetMapping("/add_theater")
    public String showTheaterAddForm(Model theModel) {
        TheaterDTO theater = new TheaterDTO();
        theModel.addAttribute("theater", theater);
        return "staff/add_theater";
    }

    @PostMapping("/adress")
    public ResponseEntity adress(Model theModel, @RequestParam("address") String address) {
        LocationResponse loation = mapService.geocode(address);
        if(loation != null){
            return ResponseEntity.ok(loation);
        }
        else{
            return ResponseEntity.badRequest().body("Could not find this " + address);
        }
    }


    @PostMapping("/add_theater")
    public String addTheater(
            @ModelAttribute("theater") TheaterDTO theaterDTO,
            @RequestParam("imageInput") MultipartFile imageFile,
            @RequestParam(value = "name", required = false) String address,
            @RequestParam(value = "latitude", required = false) String latitude,
            @RequestParam(value = "longtitude", required = false) String longtitude,
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
    @GetMapping("/search/{address}")
    public ResponseEntity searchAddress(@PathVariable String address, Model model) {
        try {
            String decodedAddress = URLDecoder.decode(address, "UTF-8");
            LocationResponse location = mapService.geocode(decodedAddress);

            if (location == null) {
                model.addAttribute("errorMessage", "Không tìm thấy địa chỉ.");
                return ResponseEntity.badRequest().body("Could not find address");
            }

            return ResponseEntity.ok(location);
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.badRequest().body("Invalid address encoding");
        }
    }

    @GetMapping("/showFormForUpdate")
    public String showFormForADd(
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
    @PostMapping("/delete")
    public String handleDeleteTheater(@RequestParam("id") Integer theaterId, RedirectAttributes redirectAttributes) {
        try {
            // Gọi service để thực hiện xóa
            theaterServiceImpl.deleteTheater(theaterId);

            // Nếu không có lỗi, gửi thông báo thành công
            redirectAttributes.addFlashAttribute("message", "Theater deleted successfully.");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            // Nếu service ném ra lỗi, gửi thông báo lỗi
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        // Luôn chuyển hướng về trang danh sách rạp hát
        return "redirect:/theater";
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
            redirectAttributes.addFlashAttribute("successMessage", "Set seat and price successfully!");
            RoomDTO room = roomServiceImpl.getRoomById(request.getRoomId());
            return "redirect:/theater/theater_detail?id=" + room.getTheaterID();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        if (theaterId == null) {
            return "redirect:/theater";
        }
        return "redirect:/theater/theater_detail?id=" + theaterId;
    }
    @GetMapping("/edit_room")
    public String showEditRoomForm(@RequestParam("roomID") Integer roomId, Model model) {
        try {
            RoomDTO roomDTO = roomServiceImpl.getRoomById(roomId);
            if (roomDTO == null) {
                return "redirect:/theater";
            }
            model.addAttribute("room", roomDTO);
            return "staff/edit_room";
        } catch (Exception e) {
            return "redirect:/theater";
        }
    }
    @PostMapping("/edit_room")
    public String updateRoom(@ModelAttribute("room") RoomDTO roomDTO, RedirectAttributes redirectAttributes) {
        try {
            roomServiceImpl.updateRoom(roomDTO);
            redirectAttributes.addFlashAttribute("message", "Room updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/theater/edit_room?roomID=" + roomDTO.getRoomID();
        }
        return "redirect:/theater/theater_detail?id=" + roomDTO.getTheaterID();
    }
    @GetMapping("/update_seat")
    public String showFormforUpdateSeat(@RequestParam("roomId") Integer roomId, Model model) {
        try {
            RoomDTO roomDTO = roomServiceImpl.getRoomById(roomId);
            if (roomDTO == null) {
                return "redirect:/theater";
            }
            SeatUpdateRequest seatUpdateRequest = new SeatUpdateRequest();
            List<Integer> vipSeatIds = roomDTO.getSeats().stream()
                    .filter(SeatDTO::getIsVIP) // Lọc những ghế có isVIP = true
                    .map(SeatDTO::getSeatID)      // Lấy ID của ghế
                    .collect(Collectors.toList()); // Thu thập thành List
            seatUpdateRequest.setVipSeatIds(vipSeatIds);
            Optional<SeatDTO> standardSeat = roomDTO.getSeats().stream()
                    .filter(seat -> !seat.getIsVIP())
                    .findFirst();
            standardSeat.ifPresent(seatDTO -> seatUpdateRequest.setNonVipPrice(seatDTO.getUnitPrice()));

            // 4. Tìm giá của một ghế VIP bất kỳ
            Optional<SeatDTO> vipSeat = roomDTO.getSeats().stream()
                    .filter(SeatDTO::getIsVIP)
                    .findFirst();
            vipSeat.ifPresent(seatDTO -> seatUpdateRequest.setVipPrice(seatDTO.getUnitPrice()));

            model.addAttribute("seatUpdateRequest", seatUpdateRequest);
            model.addAttribute("seatStatuses", Seat_Status.values());

            Map<Character, List<SeatDTO>> seatsByRow = roomDTO.getSeats().stream()
                    .collect(Collectors.groupingBy(
                            seat -> seat.getPosition().charAt(0),
                            Collectors.toList()
                    ));

            List<Character> sortedRowLabels = new ArrayList<>(seatsByRow.keySet());
            java.util.Collections.sort(sortedRowLabels);
            model.addAttribute("room", roomDTO);
            model.addAttribute("seatsByRow", seatsByRow);
            model.addAttribute("sortedRowLabels", sortedRowLabels);

            return "staff/update_seat";

        } catch (Exception e) {
            return "redirect:/theater";
        }
    }
    @PostMapping("/update_seat")
    public String handleUpdateSeats(@ModelAttribute("seatUpdateRequest") SeatUpdateRequest seatUpdateRequest,
                                    RedirectAttributes redirectAttributes) {

        Double nonVipPrice = seatUpdateRequest.getNonVipPrice();
        Double vipPrice = seatUpdateRequest.getVipPrice();
        Integer roomId = seatUpdateRequest.getRoomId();

        // --- VALIDATION LOGIC ---

        // 1. Basic price validation
        if (vipPrice == null || nonVipPrice == null || vipPrice < 0 || nonVipPrice < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Seat prices cannot be negative or empty.");
            return "redirect:/theater/update_seat?roomId=" + roomId;
        }

        // 2. VIP price must be exactly 40,000 VND more than standard price
        // We use a small epsilon for double comparison to avoid precision issues.
        if (vipPrice < nonVipPrice + 40000.0) {
            // Cập nhật lại thông báo lỗi cho phù hợp với logic mới
            redirectAttributes.addFlashAttribute("errorMessage", "VIP seat price must be at least 40,000 VND higher than the standard price.");
            return "redirect:/theater/update_seat?roomId=" + roomId;
        }

        // --- END VALIDATION ---

        try {
            // Call the service to perform the update logic
            seatServiceImpl.updateSeatsInRoom(seatUpdateRequest);

            // Add success message and redirect
            redirectAttributes.addFlashAttribute("message", "Seats updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");            // Chuyển hướng về trang chi tiết của phòng chiếu (thay vì trang chi tiết rạp)
            return "redirect:/theater/room_detail?roomID=" + seatUpdateRequest.getRoomId();
        } catch (Exception e) {
            // Log the error for debugging purposes
            // log.error("Error updating seats: ", e);

            redirectAttributes.addFlashAttribute("message", e.getMessage() + " Please try again.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/theater/room_detail?roomID=" + seatUpdateRequest.getRoomId();
        }
    }
}
