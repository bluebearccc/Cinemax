package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.RoomDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.staff.RoomServiceImpl;
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
}
