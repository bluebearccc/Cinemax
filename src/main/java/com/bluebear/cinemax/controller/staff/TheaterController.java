package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.staff.RoomServiceImpl;
import com.bluebear.cinemax.service.staff.SeatService;
import com.bluebear.cinemax.service.staff.TheaterService;
import com.bluebear.cinemax.service.staff.TheaterServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
}
